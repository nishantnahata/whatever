"""Draws the launcher icon (a pint of beer on an amber-cream tile) at every density."""
import os, sys
from PIL import Image, ImageDraw

SIZES = {"mdpi": 48, "hdpi": 72, "xhdpi": 96, "xxhdpi": 144, "xxxhdpi": 192}
S = 768  # draw large, downscale for smooth edges

def draw():
    img = Image.new("RGBA", (S, S), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    d.rounded_rectangle([24, 24, S - 24, S - 24], radius=170, fill=(42, 31, 20, 255))
    # glass outline points (trapezoid)
    top_y, bot_y = 170, 640
    tl, tr, bl, br = 230, 538, 270, 498
    def x_at(y, left):
        t = (y - top_y) / (bot_y - top_y)
        return (tl + (bl - tl) * t) if left else (tr + (br - tr) * t)
    beer_y, foam_y = 290, 215
    d.polygon([(x_at(beer_y, 1), beer_y), (x_at(beer_y, 0), beer_y), (br, bot_y), (bl, bot_y)], fill=(224, 154, 31, 255))
    d.polygon([(x_at(foam_y, 1), foam_y), (x_at(foam_y, 0), foam_y), (x_at(beer_y, 0), beer_y), (x_at(beer_y, 1), beer_y)], fill=(255, 247, 230, 255))
    for cx, cy, r in [(300, 210, 48), (370, 190, 58), (450, 200, 52), (510, 220, 36)]:
        d.ellipse([cx - r, cy - r, cx + r, cy + r], fill=(255, 247, 230, 255))
    # bubbles
    for cx, cy, r in [(330, 420, 12), (420, 360, 9), (460, 500, 11), (360, 560, 8)]:
        d.ellipse([cx - r, cy - r, cx + r, cy + r], fill=(246, 196, 92, 255))
    d.line([(tl, top_y), (bl, bot_y), (br, bot_y), (tr, top_y)], fill=(245, 234, 216, 255), width=18, joint="curve")
    # handle
    d.arc([430, 300, 620, 520], start=-90, end=90, fill=(245, 234, 216, 255), width=22)
    return img

big = draw()
out = sys.argv[1]
for name, px in SIZES.items():
    p = os.path.join(out, f"mipmap-{name}")
    os.makedirs(p, exist_ok=True)
    big.resize((px, px), Image.LANCZOS).save(os.path.join(p, "ic_launcher.png"))
big.resize((512, 512), Image.LANCZOS).save(os.path.join(out, "..", "icon-512.png"))
