package up.edu.cg.service;

import up.edu.cg.model.ImageModel;

import java.util.ArrayDeque;
import java.util.Deque;

public class ImageHistory {

    //que to store changes of the image
    private final Deque<ImageModel> undoStack = new ArrayDeque<>();

    public void push(ImageModel state) {
        undoStack.push(state);
    }

    public ImageModel undo() {
        if (undoStack.isEmpty()) return null;
        return undoStack.pop();
    }

    public boolean canUndo() {
        return !undoStack.isEmpty();
    }

    //needed for opening new files, previously when i opened files, the history was the history of the prevous image, this function helps me to empty the que
    public void clear() {
        undoStack.clear();
    }
}