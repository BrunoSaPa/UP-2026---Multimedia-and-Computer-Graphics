# Travel Video Builder

Java pipeline that turns a folder of photos/videos into a narrated vertical travel video.

## Environment

Required:

```bash
export OPENAI_API_KEY="key"
```

Optional:

```bash
export OPENAI_TEXT_MODEL="gpt-4.1-mini"
export OPENAI_IMAGE_MODEL="gpt-image-1"
export OPENAI_TTS_MODEL="gpt-4o-mini-tts"
export OPENAI_TTS_VOICE="alloy"
export OPENAI_MAX_RETRIES="3"
export OPENAI_RETRY_BACKOFF_MS="1500"
```

## Build

```bash
cd Project
rm -rf out
mkdir -p out
javac -d out src/Main.java src/GuiMain.java src/model/*.java src/service/*.java src/pipeline/*.java src/util/*.java
```

## Run (CLI)

```bash
cd Project
java -cp out Main /absolute/path/to/input /absolute/path/to/output.mp4
```

If the output path is omitted, the program writes `travel_video.mp4` inside the input directory.


## Run (GUI)

```bash
cd Project
java -cp out GuiMain
```

A Swing window will open where you can:
1. **Select Folder** – pick the directory containing your photos/videos.
2. **Preview** – thumbnails of detected media are shown (video frames extracted via ffmpeg).
3. **Build Video** – starts the 10-step pipeline with a live progress bar.

The finished video is saved as `travel_video.mp4` inside the selected folder.



