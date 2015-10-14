package com.example.android.testing.notes.notedetails;

import android.support.annotation.Nullable;

/**
 * This specifies the contract between the view and the presenter.
 */
public interface NoteDetailContract {

    interface View {

        void setProgressIndicator(boolean active);

        void showMissingNote();

        void hideTitle();

        void showTitle(String title);

        void showImage(String imageUrl);

        void hideImage();

        void hideDescription();

        void showDescription(String description);
    }

    interface UserActionsListener {

        void openNote(@Nullable String noteId);
    }
}
