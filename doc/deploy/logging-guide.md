# SenSketch logging rules

## Log tag

Application logs must use the project name as the single Android log tag:

- `SenSketch`

Do not create feature-specific tags such as `Restore` or `Playback`.
Keep the tag unchanged when adding diagnostic logs.

## Shared tags

The capture batch also includes these shared platform tags:

- `AndroidRuntime`
- `chromium`

## Capture

`capture-restore-log.bat` captures `SenSketch`, `AndroidRuntime`, and `chromium` logs.
