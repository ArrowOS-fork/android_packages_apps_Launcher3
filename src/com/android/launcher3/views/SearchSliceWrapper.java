/*
 * Copyright (C) 2020 The Android Open Source Project
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
package com.android.launcher3.views;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.slice.Slice;
import androidx.slice.SliceItem;
import androidx.slice.widget.EventInfo;
import androidx.slice.widget.SliceLiveData;
import androidx.slice.widget.SliceView;

import com.android.launcher3.Launcher;
import com.android.launcher3.allapps.search.SearchEventTracker;
import com.android.systemui.plugins.shared.SearchTarget;
import com.android.systemui.plugins.shared.SearchTargetEvent;

/**
 * A Wrapper class for {@link SliceView} search results
 */
public class SearchSliceWrapper implements SliceView.OnSliceActionListener {

    private static final String TAG = "SearchSliceController";
    private static final String URI_EXTRA_KEY = "slice_uri";


    private final Launcher mLauncher;
    private final SearchTarget mSearchTarget;
    private final SliceView mSliceView;
    //TODO: remove these as we move to tracking search results individually with unique ID
    private final int mPosition;
    private final String mSessionId;
    private LiveData<Slice> mSliceLiveData;

    public SearchSliceWrapper(Context context, SliceView sliceView,
            SearchTarget searchTarget, String sessionId, int position) {
        mLauncher = Launcher.getLauncher(context);
        mPosition = position;
        mSessionId = sessionId;
        mSearchTarget = searchTarget;
        mSliceView = sliceView;
        sliceView.setOnSliceActionListener(this);
        try {
            mSliceLiveData = SliceLiveData.fromUri(mLauncher, getSliceUri());
            mSliceLiveData.observe((Launcher) mLauncher, sliceView);
        } catch (Exception ex) {
            Log.e(TAG, "unable to bind slice", ex);
        }
    }

    /**
     * Unregisters event handlers and removes lifecycle observer
     */
    public void destroy() {
        mSliceView.setOnSliceActionListener(null);
        mSliceLiveData.removeObservers(mLauncher);
    }

    @Override
    public void onSliceAction(@NonNull EventInfo info, @NonNull SliceItem item) {
        SearchTargetEvent searchTargetEvent = new SearchTargetEvent(
                SearchTarget.ItemType.SETTINGS_SLICE,
                SearchTargetEvent.CHILD_SELECT, mPosition,
                mSessionId);
        searchTargetEvent.bundle = new Bundle();
        searchTargetEvent.bundle.putParcelable(URI_EXTRA_KEY, getSliceUri());
        SearchEventTracker.INSTANCE.get(mLauncher).notifySearchTargetEvent(searchTargetEvent);
    }

    private Uri getSliceUri() {
        return mSearchTarget.bundle.getParcelable(URI_EXTRA_KEY);
    }
}
