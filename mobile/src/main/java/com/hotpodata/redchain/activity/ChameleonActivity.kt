package com.hotpodata.redchain.activity

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.app.ActivityManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.support.v7.app.AppCompatActivity
import com.hotpodata.redchain.R

/**
 * Created by jdrotos on 11/11/15.
 */
open class ChameleonActivity : AppCompatActivity() {

    //UI stuff
    private var mLastBarColor = -1
    private var mResumed = false
    private var mAbBackgroundDrawable: Drawable? = null
    private var mActionToolbarBackgroundDrawable: Drawable? = null
    private var mAbSplitBackgroundDrawable: Drawable? = null
    private var mBarColorAnimator: ValueAnimator? = null

    public override fun onResume() {
        super.onResume()
        mResumed = true
    }

    public override fun onPause() {
        super.onPause()
        mResumed = false
    }

    fun setColor(color: Int, animate: Boolean) {
        if (mLastBarColor == -1) {
            mLastBarColor = color
        }
        if (animate && mResumed && color != mLastBarColor) {
            mBarColorAnimator?.cancel()
            val animator = ValueAnimator.ofObject(ArgbEvaluator(), mLastBarColor, color).setDuration(500)
            animator.addUpdateListener { animation ->
                (mAbBackgroundDrawable as? ColorDrawable)?.color = animation.animatedValue as Int
                (mAbSplitBackgroundDrawable as? ColorDrawable)?.color = animation.animatedValue as Int
                (mActionToolbarBackgroundDrawable as? ColorDrawable)?.color = animation.animatedValue as Int
                updateStatusBarColor(animation.animatedValue as Int)
            }
            animator.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    updateActionBarColors(color)
                    updateStatusBarColor(color)
                }
            })
            mBarColorAnimator = animator
            animator.start()
        } else if ((!animate || !mResumed) && (mBarColorAnimator == null || !mBarColorAnimator!!.isRunning)) {
            updateActionBarColors(color)
            updateStatusBarColor(color)
        }
    }


    protected fun updateActionBarColors(color: Int) {
        if (mAbBackgroundDrawable == null || mAbSplitBackgroundDrawable == null) {
            val abBackground = ColorDrawable(color)
            val abSplitBackground = ColorDrawable(color)
            val actionToolbarBg = ColorDrawable(color)
            if (mAbBackgroundDrawable?.bounds?.width()?.let { it > 0 } ?: false) {
                abBackground.bounds = mAbBackgroundDrawable?.bounds
            }
            if (mAbSplitBackgroundDrawable?.bounds?.width()?.let { it > 0 } ?: false) {
                abSplitBackground.bounds = mAbSplitBackgroundDrawable?.bounds
            }
            if (mActionToolbarBackgroundDrawable?.bounds?.width()?.let { it > 0 } ?: false) {
                actionToolbarBg.bounds = mActionToolbarBackgroundDrawable?.bounds
            }
            mAbBackgroundDrawable = abBackground
            mAbSplitBackgroundDrawable = abSplitBackground
            mActionToolbarBackgroundDrawable = actionToolbarBg
        } else {
            (mAbBackgroundDrawable as? ColorDrawable)?.color = color
            (mAbSplitBackgroundDrawable as? ColorDrawable)?.color = color
            (mActionToolbarBackgroundDrawable as? ColorDrawable)?.color = color
        }

        supportActionBar?.apply {
            setBackgroundDrawable(mAbBackgroundDrawable)
            setSplitBackgroundDrawable(mAbSplitBackgroundDrawable)
            invalidateOptionsMenu()
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            var title = title?.toString() ?: getString(R.string.app_name)
            setTaskDescription(ActivityManager.TaskDescription(title, null, color))
        }
        mLastBarColor = color
    }

    private fun updateStatusBarColor(baseColor: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val hsv = FloatArray(3)
            var darkerColor = baseColor
            Color.colorToHSV(darkerColor, hsv)
            hsv[2] *= 0.8f // value component
            darkerColor = Color.HSVToColor(hsv)
            val window = window
            if (window != null) {
                window.statusBarColor = darkerColor
            }
        }
    }
}