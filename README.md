# Multimedia and Computer Graphics (UP 2026)

**Universidad Panamericana**  
Course repository for **Multimedia and Computer Graphics**.

This repo documents a progression of projects from foundational raster/vector graphics exercises to complete multimedia pipelines.  
It reflects the creative and technical style that Universidad Panamericana showcases in contexts like **SIGGRAPH**: combining math, coding, visual design, and storytelling.

---

## Repository Structure

- `ClassSessions/`
  - In-class exploratory exercises and demos.
- `ClassWork/`
  - Guided assignments focused on core graphics concepts.
- `Homeworks/`
  - Individual tasks and algorithm-oriented implementations.
- `Midterms/`
  - Larger integration projects with full architecture and reusable modules.

---

## Current Projects (Explained)

### 1) Session 03 - First image synthesis steps
**Path:** `ClassSessions/Session03_20260127`

- Basic pixel-level image generation with Java `BufferedImage`.
- Geometric split rendering (two-color diagonal composition).
- Introductory setup project in the same session folder.

**Concepts:** raster space, coordinate systems, pixel writing.

---

### 2) Classwork 03 - SVG generation
**Path:** `ClassWork/ClassWork_03`

- Programmatic creation of `.svg` files from Java strings.
- Includes geometric primitives (`polygon`, `line`, `path`, `circle`, `rect`).
- Demonstrates vector graphics workflow and scene composition.

**Concepts:** vector graphics, scalable assets, procedural scene definition.

---

### 3) Classwork 01 - Procedural 2D scenes
**Path:** `ClassWork/Classwork01`

Contains multiple mini-projects:

- `up/edu/cg/clock`: analog clock rendered with geometry and interpolation.
- `up/edu/cg/gradient`: sunset-like gradient and stylized silhouettes.
- `up/edu/cg/waves`: sun rays + sinusoidal grass landscape.

**Concepts:** trigonometry, interpolation, implicit shapes, color composition.

---

### 4) Classwork 02 - Triangle rasterization and color interpolation
**Path:** `ClassWork/Classwork02`

- Custom classes for points, colors, vertices, and triangles.
- Barycentric coordinate computation.
- Per-pixel inside-triangle test and smooth color interpolation.

**Concepts:** barycentric math, rasterization pipeline basics, fragment coloring.

---

### 5) Classwork 05 - Text translation tool using OpenAI
**Path:** `ClassWork/Classwork_05`

- Reads a `.txt` file, asks for a target language, and translates content.
- Uses an OpenAI chat-completions request through `curl`.
- Writes translated output to a language-suffixed file.

**Concepts:** API integration, JSON payload construction, file processing automation.

---

### 6) Homework 01 - Geometry calculator
**Path:** `Homeworks/H_01`

- CLI calculator for perimeter and area.
- Supports square, rectangle, triangle, circle, pentagon, and semicircle.

**Concepts:** OOP basics, formulas, polymorphism through abstract shape class.

---

### 7) Homework 02 - Utility exercises
**Path:** `Homeworks/H_02`

- Task 3: Aspect ratio simplification using GCD.
- Task 4: Polar/cartesian coordinate conversion.
- Includes an IDE template starter file used as baseline.

**Concepts:** Euclidean algorithm, coordinate transforms, console interaction.

---

### 8) Homework 03 - Image compression with vector quantization
**Path:** `Homeworks/H_03`

- Block-based image compression pipeline.
- K-means clustering to build a codebook.
- L1 distance matching for encoding blocks to indices.
- Reconstruction (decompression) from quantized representation.

**Concepts:** vector quantization, clustering, lossy compression, image reconstruction.

---

### 9) 1st Midterm - JavaFX Image Editor
**Path:** `Midterms/1st-Midterm`

- Full JavaFX application architecture (`ui`, `controller`, `model`, `operations`, `service`).
- Image operations include inversion, rotation, and crop.
- Maven-based project with JavaFX controls/swing dependencies.

**Concepts:** desktop UI architecture, MVC-style separation, image operations pipeline.

---

### 10) 2nd Midterm - Travel Video Builder
**Path:** `Midterms/2nd-Midterm/Project`

- End-to-end multimedia pipeline that transforms media folders into narrated vertical videos.
- Integrates metadata extraction, map generation, AI text/image support, TTS narration, and FFmpeg rendering.
- Structured by domain packages (`model`, `pipeline`, `service`, `util`).

**Concepts:** multimedia orchestration, AI-assisted content generation, video post-processing.

---

## Tech Stack Used Across the Repo

- **Language:** Java
- **Graphics APIs:** Java2D (`BufferedImage`), SVG generation
- **Desktop UI:** JavaFX
- **Build tools:** Maven (midterm projects), IntelliJ module projects (`.iml`)
- **Multimedia tooling:** FFmpeg, ExifTool (in pipeline project)
- **AI integration:** OpenAI APIs in selected projects

---

## Why This Repository Matters

This repository is not a single app; it is a **learning trajectory**:

1. Pixel and vector fundamentals.
2. Mathematical graphics techniques.
3. Image processing and compression.
4. Full multimedia system design.

That progression is exactly what makes the class compelling: it links theory and implementation in a way that is exhibition-ready and aligned with the visual-computing culture represented at SIGGRAPH.

---

## Notes

- Some subprojects have their own `README.md` with local build/run instructions.

