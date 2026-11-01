"""Losslessly recompress the four shipped arena PNGs; preserve pixels and metadata."""
import hashlib
import json
from pathlib import Path
import struct
import zlib


def chunks(data):
    assert data[:8] == b"\x89PNG\r\n\x1a\n"
    offset = 8
    result = []
    while offset < len(data):
        size = struct.unpack_from(">I", data, offset)[0]
        tag = data[offset + 4:offset + 8]
        payload = data[offset + 8:offset + 8 + size]
        crc = struct.unpack_from(">I", data, offset + 8 + size)[0]
        assert zlib.crc32(tag + payload) == crc
        result.append((tag, payload))
        offset += size + 12
    assert offset == len(data) and result[-1][0] == b"IEND"
    return result


for stage in ("forest", "seles"):
    for name, manifest in (("arena.png", "arena-asset.json"), ("backdrop.png", "asset.json")):
        path = Path("gfx/battle") / stage / name
        before = path.read_bytes()
        original = chunks(before)
        pixels = zlib.decompress(b"".join(value for tag, value in original if tag == b"IDAT"))
        compressed = zlib.compress(pixels, 9)
        result = bytearray(before[:8])
        written = False
        for tag, payload in original:
            if tag == b"IDAT":
                if written:
                    continue
                payload = compressed
                written = True
            result.extend(struct.pack(">I", len(payload)) + tag + payload)
            result.extend(struct.pack(">I", zlib.crc32(tag + payload)))
        verified = chunks(result)
        assert zlib.decompress(b"".join(value for tag, value in verified if tag == b"IDAT")) == pixels
        assert [(t, p) for t, p in original if t != b"IDAT"] == [(t, p) for t, p in verified if t != b"IDAT"]
        if len(result) < len(before):
            temporary = path.with_suffix(".png.tmp")
            temporary.write_bytes(result)
            temporary.replace(path)
        metadata_path = path.with_name(manifest)
        metadata = json.loads(metadata_path.read_text(encoding="utf-8"))
        metadata["sha256"] = hashlib.sha256(path.read_bytes()).hexdigest()
        metadata["compression"] = "Lossless zlib level 9; PNG image data and non-IDAT metadata preserved"
        metadata_path.write_text(json.dumps(metadata, indent=2) + "\n", encoding="utf-8")
        print(f"{path}: {len(before):,} -> {path.stat().st_size:,} bytes; exact image data verified")
