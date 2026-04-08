package model;

//type of media content
public enum MediaType {
    PHOTO,VIDEO;

    public static MediaType fromFileName(String fileName) {
        String lower = fileName.toLowerCase();
        if (lower.endsWith(".mp4") || lower.endsWith(".mov") || lower.endsWith(".avi") || lower.endsWith(".mkv") || lower.endsWith(".wmv") || lower.endsWith(".webm")) {
            return VIDEO;
        }
        return PHOTO;
    }
}

