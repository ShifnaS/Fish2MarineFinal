/*
 * Copyright 2016 Cutesys Technologies Pvt Ltd as an unpublished work. All Rights
 * Reserved.
 *
 * The information contained herein is confidential property of Cutesys Technologies
 * Pvt Ltd. The use, copying,transfer or disclosure of such information is prohibited
 * except by express written agreement with Company.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the
 * License at http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 *
 * File Name 					: Switcher
 * Since 						: 08/11/16
 * Version Code & Project Name	: v 1.0 sponsermasterlibrary
 * Author Name					: Athira Santhosh
 */

package com.smacon.f2mlibrary.Switcher;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static com.smacon.f2mlibrary.Switcher.Preconditions.checkNotNull;

/**
 * Created by athira on  08/11/16.
 */
public class Switcher {

    private List<View> contentViews = new ArrayList<>();
    private List<View> errorViews = new ArrayList<>();
    private List<View> emptyViews = new ArrayList<>();
    private List<View> progressViews = new ArrayList<>();

    private TextView errorLabel, emptyLabel;
    private TextView progressLabel;
    private int animDuration = 300;
    private boolean errorShown;

    private List<Animations.FadeListener> runningAnimators = new ArrayList<>();

    private Switcher() {

    }

    private void addContentView(View contentView) {
        this.contentViews.add(contentView);
    }

    private void addErrorView(View errorView) {
        this.errorViews.add(errorView);
    }

    private void addEmptyView(View emptyView) {
        this.emptyViews.add(emptyView);
    }


    private void addProgressView(View progressView) {
        this.progressViews.add(progressView);
    }

    private void setErrorLabel(TextView errorLabel) {
        this.errorLabel = errorLabel;
    }

    private void setEmptyLabel(TextView emptyLabel) {
        this.emptyLabel = emptyLabel;
    }

    private void setProgressLabel(TextView progressLabel) {
        this.progressLabel = progressLabel;
    }

    private void setAnimationDuration(int duration) {
        animDuration = duration;
    }

    public static class Builder {

        private Switcher switcher = new Switcher();
        private Context context;

        public Builder(Context context) {
            this.context = context;
            switcher.setAnimationDuration(context.getResources().getInteger(android.R.integer.config_shortAnimTime));
        }

        public Builder addContentView(View contentView) {
            checkNotNull(contentView, "Provided content view is null");
            switcher.addContentView(contentView);
            return this;
        }

        public Builder addErrorView(View errorView) {
            checkNotNull(errorView, "Provided error view is null");
            switcher.addErrorView(errorView);
            return this;
        }

        public Builder addEmptyView(View emptyView) {
            checkNotNull(emptyView, "Provided empty view is null");
            switcher.addEmptyView(emptyView);
            return this;
        }

        public Builder addProgressView(View progressView) {
            checkNotNull(progressView, "Provided progress view is null");
            switcher.addProgressView(progressView);
            return this;
        }

        public Builder setProgressLabel(TextView progressLabel) {
            checkNotNull(progressLabel, "Provided progress label is null");
            switcher.setProgressLabel(progressLabel);
            return this;
        }

        public Builder setEmptyLabel(TextView emptyLabel) {
            checkNotNull(emptyLabel, "Provided progress label is null");
            switcher.setEmptyLabel(emptyLabel);
            return this;
        }

        public Builder setErrorLabel(TextView errorLabel) {
            checkNotNull(errorLabel, "Provided error label is null");
            switcher.setErrorLabel(errorLabel);
            return this;
        }

        public Switcher build() {
            switcher.setupViews();
            return switcher;
        }
    }

    private void setupViews() {
        for (View contentView : contentViews) {
            contentView.setVisibility(View.VISIBLE);
        }

        for (View errorView : errorViews) {
            errorView.setVisibility(View.INVISIBLE);
        }

        for (View progressView : progressViews) {
            progressView.setVisibility(View.INVISIBLE);
        }

        for (View emptyView : emptyViews) {
            emptyView.setVisibility(View.INVISIBLE);
        }
    }

    private List<View> membersExcludingGroup(List<View> excludedGroup) {
        List<View> members = new ArrayList<>();
        members.addAll(contentViews);
        members.addAll(errorViews);
        members.addAll(progressViews);
        members.addAll(emptyViews);

        if (excludedGroup != null && excludedGroup.size() > 0) {
            members.removeAll(excludedGroup);
        }

        return members;
    }

    private boolean isViewPartOfSwitcher(View potentialMember) {
        List<View> members = new ArrayList<>();
        members.addAll(contentViews);
        members.addAll(errorViews);
        members.addAll(progressViews);
        members.addAll(emptyViews);

        for (View view : members) {
            if (view.equals(potentialMember)) return true;
        }
        return false;
    }

    private void showGroup(List<View> groupToShow, boolean immediately) {
        if (groupToShow.equals(errorViews)) errorShown = true;
        else errorShown = false;

        cancelCurrentAnimators();

        for (View view : groupToShow) {
            if (immediately) {
                view.setVisibility(View.VISIBLE);
            } else {
                Animations.FadeListener animator = Animations.fadeIn(view, animDuration);
                if (animator != null) runningAnimators.add(animator);
            }
        }

        List<View> viewsToHide = membersExcludingGroup(groupToShow);
        for (View view : viewsToHide) {
            if (immediately) {
                view.setVisibility(View.INVISIBLE);
            } else {
                Animations.FadeListener animator = Animations.fadeOut(view, animDuration);
                if (animator != null) runningAnimators.add(animator);
            }
        }
    }

    public void showContentView() {
        Log.i(Switcher.class.getSimpleName(), "showContentView");
        showGroup(contentViews, false);
    }

    public void showContentViewImmediately() {
        Log.i(Switcher.class.getSimpleName(), "showContentViewImmediately");
        showGroup(contentViews, true);
    }


    public void showProgressView() {
        Log.i(Switcher.class.getSimpleName(), "showProgressView");
        showGroup(progressViews, false);
    }

    public void showProgressViewImmediately() {
        Log.i(Switcher.class.getSimpleName(), "showProgressViewImmediately");
        showGroup(progressViews, true);
    }

    public void showProgressView(String progressMessage) {
        if (errorLabel == null) {
            throw new NullPointerException("You have to build Switcher using withProgressLabel() method");
        }

        progressLabel.setText(progressMessage);
        showProgressView();
    }

    public void showErrorView() {
        Log.i(Switcher.class.getSimpleName(), "showErrorView");
        showGroup(errorViews, false);
    }

    public void showErrorViewImmediately() {
        Log.i(Switcher.class.getSimpleName(), "showErrorView");
        showGroup(errorViews, true);
    }

    public void showErrorView(String errorMessage) {
        if (errorLabel == null) {
            throw new NullPointerException("You have to build Switcher using withErrorLabel() method");
        }

        errorLabel.setText(errorMessage);
        showErrorView();
    }

    public void showErrorView(final OnErrorViewListener listener) {
        for (View errorView : errorViews) {
            errorView.setClickable(true);
            errorView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    listener.onErrorViewClicked();
                    view.setOnClickListener(null);
                }
            });
        }
        showErrorView();
    }

    public void showErrorView(String errorMessage, final OnErrorViewListener listener) {
        if (errorLabel == null) {
            throw new NullPointerException("You have to build Switcher using withErrorLabel() method");
        }

        errorLabel.setText(errorMessage);
        showErrorView(listener);
    }

    public void showEmptyView() {
        Log.i(Switcher.class.getSimpleName(), "showEmptyView");
        showGroup(emptyViews, false);
    }

    public void showEmptyViewImmediately() {
        Log.i(Switcher.class.getSimpleName(), "showEmptyView");
        showGroup(emptyViews, true);
    }

    private void cancelCurrentAnimators() {
        Iterator<Animations.FadeListener> it = runningAnimators.iterator();
        while (it.hasNext()) {
            Animations.FadeListener animator = it.next();
            animator.onAnimationEnd();
            it.remove();
        }
    }

    public boolean isShowingError() {
        return errorShown;
    }
}
