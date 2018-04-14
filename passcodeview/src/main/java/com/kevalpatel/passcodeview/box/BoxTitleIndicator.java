/*
 * Copyright (c) 2018. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package com.kevalpatel.passcodeview.box;

import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.util.AttributeSet;

import com.kevalpatel.passcodeview.Constants;
import com.kevalpatel.passcodeview.R;
import com.kevalpatel.passcodeview.Utils;
import com.kevalpatel.passcodeview.indicators.Indicator;
import com.kevalpatel.passcodeview.internal.BasePasscodeView;

import java.util.ArrayList;

/**
 * Created by Keval Patel on 09/04/17.
 * This {@link Box} is to display title and passcode indicator. The number of the passcode indicator
 * will depend on the length of the passcode indicator.
 *
 * @author 'https://github.com/kevalpatel2106'
 */

public final class BoxTitleIndicator extends Box {
    private static final long ERROR_ANIMATION_DURATION = 400;
    /**
     * Default title text.
     */
    private static final String DEF_TITLE_TEXT = "Enter PIN";

    /**
     * Size of the PIN.
     */
    private int mPinLength;

    /**
     * Size of the currently typed PIN.
     */
    private int mTypedPinLength;

    /**
     * Color of the title text.
     */
    @ColorInt
    private int mTitleColor;
    /**
     * Text of the title.
     */
    private String mTitle;

    /**
     * {@link android.text.TextPaint} of the title with {@link #mTitleColor} ast thetext color.
     */
    private Paint mTitlePaint;

    /**
     * List of all the {@link Indicator}. The length of this array will be same as {@link #mPinLength}.
     */
    private ArrayList<Indicator> mIndicators;


    private Rect mDotsIndicatorBound;
    private Indicator.Builder mIndicatorBuilder;

    public BoxTitleIndicator(@NonNull final BasePasscodeView view) {
        super(view);
    }

    @Override
    public void init() {
        //TODO init
    }

    @SuppressWarnings("deprecation")
    @Override
    public void setDefaults() {
        mTitle = DEF_TITLE_TEXT;
        mTitleColor = getContext().getResources().getColor(R.color.lib_key_default_color);
    }

    @Override
    public void parseTypeArr(@NonNull final AttributeSet typedArray) {
        TypedArray a = getContext().getTheme().obtainStyledAttributes(typedArray, R.styleable.PinView, 0, 0);
        try {
            //Parse title params
            mTitle = a.hasValue(R.styleable.PinView_pin_titleText) ?
                    a.getString(R.styleable.PinView_pin_titleText) : DEF_TITLE_TEXT;
            mTitleColor = a.getColor(R.styleable.PinView_pin_titleTextColor,
                    Utils.getColorCompat(getContext(), R.color.lib_key_default_color));
        } finally {
            a.recycle();
        }
    }

    @Override
    public void onAuthenticationFail() {
        //Set indicator to error
        new android.os.Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                for (Indicator indicator : mIndicators) indicator.onAuthFailed();
                getRootView().invalidate();
            }
        }, ERROR_ANIMATION_DURATION);
    }

    @Override
    public void onAuthenticationSuccess() {
        //Set indicator to success
        new android.os.Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                for (Indicator indicator : mIndicators) indicator.onAuthSuccess();
                getRootView().invalidate();
            }
        }, ERROR_ANIMATION_DURATION);
    }

    @Override
    public void reset() {
        //TODO Reset
    }

    @Override
    public void drawView(@NonNull final Canvas canvas) {
        if (mIndicatorBuilder == null)
            throw new NullPointerException("Build indicator before using it.");

        canvas.drawText(mTitle,
                mDotsIndicatorBound.exactCenterX(),
                mDotsIndicatorBound.top - (int) getContext().getResources().getDimension(R.dimen.lib_divider_vertical_margin),
                mTitlePaint);

        for (int i = 0; i < mPinLength; i++)
            mIndicators.get(i).draw(canvas, i < mTypedPinLength);
    }

    /**
     * |------------------------|=|
     * |                        | |
     * |                        | |
     * |         TITLE          | | => 2 * {@link com.kevalpatel.passcodeview.R.dimen#lib_divider_vertical_margin} px up from the bottom of the indicators.
     * |   |===============|    | |
     * |   |   INDICATORS  |    | |
     * |   |===============|    | | => 2 * {@link com.kevalpatel.passcodeview.R.dimen#lib_divider_vertical_margin} px up from the bottom key board
     * |------------------------|=| => {@link Constants#KEY_BOARD_TOP_WEIGHT} of the total height.
     * |                        | |
     * |                        | |
     * |                        | | => Keypad height. ({@link BoxKeypad#measureView(Rect)})
     * |                        | |
     * |                        | |
     * |------------------------|=| => {@link Constants#KEY_BOARD_BOTTOM_WEIGHT} of the total weight if the fingerprint is available. Else it touches to the bottom of the main view.
     * |                        | | => Section for fingerprint. If the fingerprint is enabled. Otherwise keyboard streaches to the bottom of the root view.
     * |------------------------|=|
     * Don't change until you know what you are doing. :-)
     *
     * @param rootViewBounds {@link Rect} bounds of the main view.
     */
    @Override
    public void measureView(@NonNull final Rect rootViewBounds) {
        int indicatorWidth = (int) (mIndicatorBuilder.getIndicatorWidth() + 2 * getContext().getResources().getDimension(R.dimen.lib_indicator_padding));
        int totalSpace = indicatorWidth * mPinLength;

        //Calculate the bound of this box.
        mDotsIndicatorBound = new Rect();
        mDotsIndicatorBound.left = (rootViewBounds.width() - totalSpace) / 2;
        mDotsIndicatorBound.right = mDotsIndicatorBound.left + totalSpace;
        mDotsIndicatorBound.bottom = rootViewBounds.top
                + (int) (rootViewBounds.height() * Constants.KEY_BOARD_TOP_WEIGHT
                - 2 * getContext().getResources().getDimension(R.dimen.lib_divider_vertical_margin));
        mDotsIndicatorBound.top = mDotsIndicatorBound.bottom - indicatorWidth;

        //Prepare all the indicators.
        mIndicators = new ArrayList<>();
        for (int i = 0; i < mPinLength; i++) {
            Rect rect = new Rect();
            rect.left = mDotsIndicatorBound.left + i * indicatorWidth;
            rect.right = rect.left + indicatorWidth;
            rect.top = mDotsIndicatorBound.top;
            rect.bottom = mDotsIndicatorBound.bottom;
            mIndicators.add(mIndicatorBuilder.buildInternal(rect));
        }
    }

    @Override
    public void preparePaint() {
        //Set title paint
        mTitlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mTitlePaint.setColor(mTitleColor);
        mTitlePaint.setTextAlign(Paint.Align.CENTER);
        mTitlePaint.setTextSize(getContext().getResources().getDimension(R.dimen.lib_title_text_size));
    }

    public void onPinDigitEntered(final int newLength) {
        mTypedPinLength = newLength;
    }

    public void setPinLength(final int pinLength) {
        mPinLength = pinLength;
    }

    @NonNull
    public String getTitle() {
        return mTitle;
    }

    public void setTitle(@NonNull final String title) {
        this.mTitle = title;
    }

    @ColorInt
    public int getTitleColor() {
        return mTitleColor;
    }

    public void setTitleColor(@ColorInt final int titleColor) {
        this.mTitleColor = titleColor;
        preparePaint();
    }

    public Indicator.Builder getIndicatorBuilder() {
        return mIndicatorBuilder;
    }

    public void setIndicatorBuilder(@NonNull final Indicator.Builder mIndicatorBuilder) {
        this.mIndicatorBuilder = mIndicatorBuilder;
    }
}
