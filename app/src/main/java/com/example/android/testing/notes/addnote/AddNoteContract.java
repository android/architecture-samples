package com.example.android.testing.notes.addnote;

import android.support.annotation.NonNull;

import java.io.IOException;

/**
 * This specifies the contract between the view and the presenter.
 */
public interface AddNoteContract {

    interface View {

        void showEmptyNoteError();

        void showNotesList();

        void openCamera(String saveTo);

        void showImagePreview(@NonNull String uri);

        void showImageError();

        void setUserActionListener(UserActionsListener listener);
    }

    interface UserActionsListener {

        void saveNote(String title, String description);

        void takePicture() throws IOException;

        void imageAvailable();

        void imageCaptureFailed();
    }
}
