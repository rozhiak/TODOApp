package com.rmblack.todoapp.fragments

import android.app.Dialog
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.LinearLayout
import android.widget.RelativeLayout
import com.allattentionhere.fabulousfilter.AAH_FabulousFragment
import com.rmblack.todoapp.R


class MySampleFabFragment: AAH_FabulousFragment() {

    companion object {
        fun newInstance(): MySampleFabFragment {
            return MySampleFabFragment()
        }
    }

    override fun setupDialog(dialog: Dialog, style: Int) {
        val contentView: View = View.inflate(context, R.layout.floating_button_sheet, null)
        val rl_content: RelativeLayout = contentView.findViewById(R.id.rl_content) as RelativeLayout
        val ll_buttons: LinearLayout = contentView.findViewById(R.id.ll_buttons) as LinearLayout


        //params to set
        setAnimationDuration(250) //optional; default 500ms
        setInterpolator(AccelerateDecelerateInterpolator()) // optional
        setPeekHeight(300) // optional; default 400dp
//        setCallbacks(activity as Callbacks?) //optional; to get back result
//        setAnimationListener(activity as AnimationListener?) //optional; to get animation callbacks
        setViewgroupStatic(ll_buttons) // optional; layout to stick at bottom on slide
//        setViewPager(vp_types) //optional; if you use viewpager that has scrollview
        setViewMain(rl_content) //necessary; main bottomsheet view
        setMainContentView(contentView) // necessary; call at end before super
        super.setupDialog(dialog, style) //call super at last
    }
}