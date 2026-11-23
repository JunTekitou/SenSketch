# SenSketch icon source

These editable SVGs mirror the current Android launcher icon:

- `sensketch-icon.svg`: full icon preview with its dark background.
- `sensketch-foreground.svg`: adaptive icon foreground; the background stays transparent.
- `sensketch-background.svg`: adaptive icon background layer.
- `sensketch-monochrome.svg`: Android themed-icon monochrome mark.

The installed Android app currently uses the corresponding VectorDrawable XML files under `app/src/main/res/drawable`. Android does not load these SVG files directly; after editing an SVG, convert its paths to VectorDrawable XML to update the installed icon.
