/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.testing.notes.util;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import android.os.Environment;

import java.io.File;
import java.io.IOException;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest({Environment.class, File.class})
public class ImageFileTest {

    @Mock
    private File mDirectory;
    @Mock
    private File mImageFile;

    private ImageFileImpl mFileHelper;

    @Before
    public void createImageFile() throws IOException {
        mFileHelper = new ImageFileImpl();
        withStaticallyMockedEnvironmentAndFileApis();
    }

    @Test
    public void create_SetsImageFile() throws IOException {
        mFileHelper.create("Name", "Extension");
        assertThat(mFileHelper.mImageFile, is(notNullValue()));
    }

    @Test
    public void deleteImageFile() {
        mFileHelper.delete();
        assertThat(mFileHelper.mImageFile, is(nullValue()));
    }

    private void withStaticallyMockedEnvironmentAndFileApis() throws IOException {
        mockStatic(Environment.class, File.class);
        when(Environment.getExternalStorageDirectory())
                .thenReturn(mDirectory);
        when(File.createTempFile(anyString(), anyString(), eq(mDirectory))).thenReturn(mImageFile);
    }
}
