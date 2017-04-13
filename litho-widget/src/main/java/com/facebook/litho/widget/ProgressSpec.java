/**
 * Copyright (c) 2017-present, Facebook, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 */

package com.facebook.litho.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.widget.ProgressBar;

import com.facebook.litho.ComponentContext;
import com.facebook.litho.ComponentLayout;
import com.facebook.litho.Output;
import com.facebook.litho.R;
import com.facebook.litho.Size;
import com.facebook.litho.SizeSpec;
import com.facebook.litho.annotations.FromPrepare;
import com.facebook.litho.annotations.MountSpec;
import com.facebook.litho.annotations.OnCreateMountContent;
import com.facebook.litho.annotations.OnLoadStyle;
import com.facebook.litho.annotations.OnMeasure;
import com.facebook.litho.annotations.OnMount;
import com.facebook.litho.annotations.OnPrepare;
import com.facebook.litho.annotations.OnUnmount;
import com.facebook.litho.annotations.Prop;
import com.facebook.litho.annotations.PropDefault;
import com.facebook.litho.annotations.ResType;
import com.facebook.litho.reference.Reference;
import com.facebook.litho.reference.ResourceDrawableReference;
import com.facebook.litho.utils.MeasureUtils;

/**
 * Renders an infinitely spinning progress bar.
 */
@MountSpec(isPureRender = true)
class ProgressSpec {

  static final int DEFAULT_SIZE = 50;

  @PropDefault static final int color = Color.TRANSPARENT;

  @OnLoadStyle
  static void onLoadStyle(
      ComponentContext c,
      Output<Reference<Drawable>> indeterminateDrawable) {

    indeterminateDrawable.set(getStyledIndeterminateDrawable(c, 0));
  }

  @OnPrepare
  static void onPrepare(
      ComponentContext c,
      @Prop(optional = true, resType = ResType.DRAWABLE_REFERENCE)
          Reference<Drawable> indeterminateDrawable,
      Output<Reference<Drawable>> resolvedIndeterminateDrawable) {
    if (indeterminateDrawable != null) {
      resolvedIndeterminateDrawable.set(indeterminateDrawable);
    } else {
      resolvedIndeterminateDrawable.set(getStyledIndeterminateDrawable(
          c,
          android.R.attr.progressBarStyle));
    }
  }

  @OnMeasure
  static void onMeasure(
      ComponentContext c,
      ComponentLayout layout,
      int widthSpec,
      int heightSpec,
      Size size) {
    if (SizeSpec.getMode(widthSpec) == SizeSpec.UNSPECIFIED &&
        SizeSpec.getMode(heightSpec) == SizeSpec.UNSPECIFIED) {
      size.width = DEFAULT_SIZE;
      size.height = DEFAULT_SIZE;
    } else {
      MeasureUtils.measureWithEqualDimens(widthSpec, heightSpec, size);
    }
  }

  @OnMount
  static void onMount(
      ComponentContext c,
      ProgressBar progressBar,
      @Prop(optional = true, resType = ResType.COLOR) int color,
      @FromPrepare Reference<Drawable> resolvedIndeterminateDrawable) {

    if (resolvedIndeterminateDrawable != null) {
      progressBar.setIndeterminateDrawable(Reference.acquire(c, resolvedIndeterminateDrawable));
    }

    if (color != Color.TRANSPARENT && progressBar.getIndeterminateDrawable() != null) {
      progressBar.getIndeterminateDrawable().mutate().setColorFilter(
          color,
          PorterDuff.Mode.MULTIPLY);
    }
  }

  @OnUnmount
  static void onUnmount(
      ComponentContext c,
      ProgressBar progressBar,
      @Prop(optional = true, resType = ResType.COLOR) int color,
      @FromPrepare Reference<Drawable> resolvedIndeterminateDrawable) {

    // restore the color first, since it acts on the indeterminateDrawable
    if (color != Color.TRANSPARENT && progressBar.getIndeterminateDrawable() != null) {
      progressBar.getIndeterminateDrawable().mutate().clearColorFilter();
    }

    if (resolvedIndeterminateDrawable != null) {
      Reference.release(c, progressBar.getIndeterminateDrawable(), resolvedIndeterminateDrawable);
    }

    progressBar.setIndeterminateDrawable(null);
  }

  @OnCreateMountContent
  static ProgressBar onCreateMountContent(ComponentContext c) {
    return new ProgressView(c);
  }

  static Reference<Drawable> getStyledIndeterminateDrawable(ComponentContext c, int defStyle) {
    Reference<Drawable> indeterminateDrawable = null;

    final TypedArray styledAttributes = c.obtainStyledAttributes(R.styleable.Progress, defStyle);

    for (int i = 0, size = styledAttributes.getIndexCount(); i < size; i++) {
      final int attr = styledAttributes.getIndex(i);

      if (attr == R.styleable.Progress_android_indeterminateDrawable) {
        indeterminateDrawable = ResourceDrawableReference.create(c)
            .resId(styledAttributes.getResourceId(attr, 0))
            .build();
      }
    }

    styledAttributes.recycle();

    return indeterminateDrawable;
  }

  private static class ProgressView extends ProgressBar {

    private ProgressView(Context context) {
      super(context);
    }

    /**
     * ProgressBar is not setting the right bounds on the drawable passed to
     * {@link ProgressBar#setIndeterminateDrawable(Drawable)}. Overriding the method and setting
     * the bounds before passing the drawable in solves the issue.
     */
    @Override
    public void setIndeterminateDrawable(Drawable d) {
      if (d != null) {
        d.setBounds(0, 0, getWidth(), getHeight());
      }

      super.setIndeterminateDrawable(d);
    }
  }
}
