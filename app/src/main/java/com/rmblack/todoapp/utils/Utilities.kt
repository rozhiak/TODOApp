package com.rmblack.todoapp.utils

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.View
import androidx.appcompat.widget.AppCompatImageView
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.google.android.material.behavior.SwipeDismissBehavior
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.snackbar.Snackbar.SnackbarLayout
import com.rmblack.todoapp.R

class Utilities {

    companion object {

        fun makeDeleteSnackBar(activity: Activity, container: View, onUndo: () -> Unit): Snackbar {
            val snackBar = Snackbar.make(container, "", Snackbar.LENGTH_LONG)
            val customSnackView: View =
                activity.layoutInflater.inflate(R.layout.delete_snack, null)
            snackBar.view.setBackgroundColor(Color.TRANSPARENT)
            val snackBarLayout = snackBar.view as SnackbarLayout
            snackBarLayout.setPadding(0, 0, 0, 0)
            val undo: AppCompatImageView = customSnackView.findViewById(R.id.undo)
            undo.setOnClickListener {
                onUndo()
                snackBar.dismiss()
            }
            snackBarLayout.addView(customSnackView, 0)
            val behavior = BaseTransientBottomBar.Behavior().apply {
                setSwipeDirection(SwipeDismissBehavior.SWIPE_DIRECTION_ANY)
            }
            snackBar.behavior = behavior
            val fab = activity.findViewById<FloatingActionButton>(R.id.fab)
            snackBar.anchorView = fab
            snackBar.show()
            return snackBar
        }

    }

    class MoveUpwardBehavior(context: Context?, attrs: AttributeSet?) :
        CoordinatorLayout.Behavior<View>(context, attrs) {

        override fun layoutDependsOn(
            parent: CoordinatorLayout,
            child: View,
            dependency: View
        ): Boolean {
            return dependency is SnackbarLayout
        }

        override fun onDependentViewChanged(
            parent: CoordinatorLayout,
            child: View,
            dependency: View
        ): Boolean {
            val translationY = minOf(0f, dependency.translationY - dependency.height)
            if (dependency.translationY != 0f)
                child.translationY = translationY
            return true
        }
    }

}