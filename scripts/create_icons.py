import base64
import os

# 1x1 pixel red png
png_base64 = "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mP8z8BQDwAEhQGAhKmMIQAAAABJRU5ErkJggg=="

# 16x16 ico (very basic)
ico_base64 = "AAABAAEAHgAAEAIAAABAAQAAFgAAACgAAAAeAAAAHgAAAAEAGAAAAAAAgAEAAAAAAAAAAAAAAAAAAAAAAAEBAQ=="

os.makedirs("src/main/resources/icons", exist_ok=True)

with open("src/main/resources/icons/icon.png", "wb") as f:
    f.write(base64.b64decode(png_base64))

with open("src/main/resources/icons/icon.ico", "wb") as f:
    f.write(base64.b64decode(ico_base64))

print("Icons created.")
