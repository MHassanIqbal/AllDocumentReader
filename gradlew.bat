/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package androidx.test.espresso;

import static androidx.test.espresso.DataInteraction.DisplayDataMatcher.displayDataMatcher;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static org.hamcrest.Matchers.allOf;

import android.support.annotation.CheckResult;
import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;
import android.view.View;
import android.view.ViewParent;
import android.widget.Adapter;
import android.widget.AdapterView;
import androidx.test.espresso.action.AdapterDataLoaderAction;
import androidx.test.espresso.action.AdapterViewProtocol;
import androidx.test.espresso.action.AdapterViewProtocol.AdaptedData;
import androidx.test.espresso.action.AdapterViewProtocols;
import androidx.test.espresso.matcher.RootMatchers;
import androidx.test.espresso.remote.ConstructorInvocation;
import androidx.test.espresso.remote.annotation.RemoteMsgConstructor;
import androidx.test.espresso.remote.annotation.RemoteMsgField;
import androidx.test.espresso.util.EspressoOptional;
import com.google.common.base.Function;
import javax.annotation.CheckReturnValue;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

/**
 * An interface to interact with data displayed in AdapterViews.
 *
 * <p>This interface builds on top of {@link ViewInteraction} and shoul