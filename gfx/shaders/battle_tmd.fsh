#version 330 core

in GS_OUT {
  smooth vec2 vertUv;
  flat vec2 vertTpage;
  flat vec2 vertClut;
  flat int vertBpp;
  smooth vec4 vertColour;
  flat int vertFlags;

  flat int translucency;

  flat float widthMultiplier;
  flat int widthMask;
  flat int indexShift;
  flat int indexMask;

  smooth float depth;
  smooth float depthOffset;
};

layout(std140) uniform projectionInfo {
  float znear;
  float zfar;
  float zdiffInv;
  float projectionMode;
};

layout(std140) uniform scissor {
  float scissorX;
  float scissorY;
  float scissorW;
  float scissorH;
};

uniform vec3 recolour;
uniform vec2 uvOffset;
uniform float discardTranslucency;
uniform int tmdTranslucency;
uniform int ctmdFlags;
uniform sampler2D tex24;
uniform sampler2D stagePalette;
uniform usampler2D tex15;

layout(location = 0) out vec4 outColour;

void main() {
  // Older Intel iGPUs are buggy and don't implement scissoring properly, causing the Shirley fight to lock up when
  // she transforms into another character. This is a workaround and reimplements scissoring at the shader level.
  if(gl_FragCoord.x < scissorX || gl_FragCoord.x >= scissorX + scissorW || gl_FragCoord.y < scissorY || gl_FragCoord.y >= scissorY + scissorH) {
    discard;
  }

  // Linearize depth for perspective transforms so that we can render ortho models at specific depths
  if(projectionMode == 2) {
    gl_FragDepth = (depth - znear) * zdiffInv + depthOffset;
  } else {
    gl_FragDepth = gl_FragCoord.z;
  }

  bool ctmd = (ctmdFlags & 0x20) != 0;
  bool uniformLit = (ctmdFlags & 0x10) != 0;
  bool translucent = (vertFlags & 0x8) != 0 || (ctmdFlags & 0x2) != 0;
  bool textured = (vertFlags & 0x2) != 0;
  outColour = vertColour;

  int translucencyMode = translucency + 1;
  if(translucent && (!textured || uniformLit)) {
    translucencyMode = tmdTranslucency + 1;
  }

  // Textured
  if(textured) {
    vec4 texColour;
    if(vertBpp == 0 || vertBpp == 1) {
      // Calculate CLUT index
      ivec2 uv = ivec2(vertTpage.x + (vertUv.x + uvOffset.x) * widthMultiplier, vertTpage.y + vertUv.y + uvOffset.y);
      ivec4 indexVec = ivec4(texelFetch(tex15, uv, 0));
      int p = (indexVec.r >> ((int(vertTpage.x + vertUv.x) & widthMask) << indexShift)) & indexMask;

      // Pull actual pixel colour from CLUT
      uint pixel = texelFetch(tex15, ivec2(vertClut.x + p, vertClut.y), 0).r;
      texColour.a = float(pixel >> 15 & 0x1fu) / 31.0;
      texColour.b = float(pixel >> 10 & 0x1fu) / 31.0;
      texColour.g = float(pixel >>  5 & 0x1fu) / 31.0;
      texColour.r = float(pixel       & 0x1fu) / 31.0;

      // Each remastered stage retains its own original animated rectangle.
      vec2 stageUv = vertUv + uvOffset;
      bool forest = (ctmdFlags & 0x80) != 0;
      bool animatedTile = forest
        ? stageUv.x >= 0.0 && stageUv.x < 64.0 && stageUv.y >= 128.0 && stageUv.y < 192.0
        : stageUv.x >= 144.0 && stageUv.x < 208.0 && stageUv.y >= 64.0 && stageUv.y < 128.0;
      // The bare tree has translucent primitives but an entirely opaque cutout palette.
      bool treeCutout = (ctmdFlags & 0x40) != 0 && all(equal(ivec2(vertClut), ivec2(448, 245)));
      if((ctmdFlags & 0xc0) != 0 && (!translucent || treeCutout) && !animatedTile) {
        vec4 surface = texture(tex24, (stageUv + vec2(0.5)) / 256.0);
        if(surface.a < 0.5) discard;
        // A shared swatch tracks live palette effects without reintroducing retail pixel blocks.
        uint swatch = texelFetch(tex15, ivec2(vertClut) + ivec2(8, 0), 0).r;
        vec3 live = vec3(swatch & 31u, swatch >> 5 & 31u, swatch >> 10 & 31u) / 31.0;
        vec3 reference = texelFetch(stagePalette, ivec2(vertClut) - ivec2(448, 240) + ivec2(8, 0), 0).rgb;
        texColour = vec4(surface.rgb * live / max(reference, vec3(1.0 / 31.0)), treeCutout ? 0.0 : 1.0);
      }
    } else if(vertBpp == 2) {
      ivec2 uv = ivec2(vertTpage.x + (vertUv.x + uvOffset.x), vertTpage.y + vertUv.y + uvOffset.y);
      uint pixel = texelFetch(tex15, uv, 0).r;
      texColour.a = float(pixel >> 15 & 0x1fu) / 31.0;
      texColour.b = float(pixel >> 10 & 0x1fu) / 31.0;
      texColour.g = float(pixel >>  5 & 0x1fu) / 31.0;
      texColour.r = float(pixel       & 0x1fu) / 31.0;
    } else {
      texColour = texture(tex24, vertUv + uvOffset);
    }

    // Discard if (0, 0, 0)
    if(texColour.a == 0 && texColour.r == 0 && texColour.g == 0 && texColour.b == 0) {
      discard;
    }

    // If translucent primitive and texture pixel translucency bit is set, pixel is translucent so we defer rendering
    if(discardTranslucency == 1 && translucent && texColour.a != 0 || discardTranslucency == 2 && (!translucent || texColour.a == 0)) {
      discard;
    }

    outColour = clamp(outColour * texColour, 0.0, 1.0);
  } else {
    // Untextured translucent primitives don't have a translucency bit so we always discard during the appropriate discard modes
    if(discardTranslucency == 1 && translucent || discardTranslucency == 2 && !translucent) {
      discard;
    }
  }

  outColour.rgb *= recolour;

  if(translucent && translucencyMode == 1) { // (B+F)/2 translucency
    outColour.a = 0.5;
  } else {
    outColour.a = 1.0;
  }
}
