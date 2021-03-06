package com.reactnativenavigation.views.slidingOverlay;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.view.View;
import android.widget.RelativeLayout;

import com.reactnativenavigation.animation.PeekingAnimator;
import com.reactnativenavigation.params.SlidingOverlayParams;
import com.reactnativenavigation.screens.Screen;
import com.reactnativenavigation.utils.ViewUtils;
import com.reactnativenavigation.views.ContentView;

public class SlidingOverlay {

    private enum VisibilityState {
        Hidden, AnimateHide, Shown, AnimateShow
    }

    private final ContentView view;
    private final RelativeLayout parent;
    private final SlidingOverlayParams params;

    private SlidingListener listener;
    private VisibilityState visibilityState = VisibilityState.Hidden;

    public interface SlidingListener {
        void onSlidingOverlayGone();
        void onSlidingOverlayShown();
    }

    public SlidingOverlay(RelativeLayout parent, SlidingOverlayParams params) {
        this.parent = parent;
        this.params = params;
        view = createSlidingOverlayView(params);
    }

    public void setSlidingListener(SlidingListener listener) {
        this.listener = listener;
    }

    public void show() {
        parent.addView(view);

        final PeekingAnimator animator = new PeekingAnimator(view, true);
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationCancel(Animator animator) {
                onSlidingOverlayShown(view);
            }

            @Override
            public void onAnimationEnd(Animator animator) {
                onSlidingOverlayShown(view);
            }
        });
        view.setOnDisplayListener(new Screen.OnDisplayListener() {
            @Override
            public void onDisplay() {
                view.setVisibility(View.VISIBLE);
                visibilityState = VisibilityState.AnimateShow;
                animator.animate();
            }
        });
    }

    public void hide() {
        final PeekingAnimator animator = new PeekingAnimator(view, false);
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationCancel(Animator animator) {
                onSlidingOverlayEnd(view);
            }

            @Override
            public void onAnimationEnd(Animator animator) {
                onSlidingOverlayEnd(view);
            }
        });

        visibilityState = VisibilityState.AnimateHide;
        animator.animate();
    }

    public boolean isShowing() {
        return VisibilityState.AnimateShow == visibilityState;
    }

    public boolean isVisible() {
        return VisibilityState.Shown == visibilityState;
    }

    public boolean isHiding() {
        return VisibilityState.AnimateHide == visibilityState;
    }

    protected ContentView createSlidingOverlayView(SlidingOverlayParams params) {
        final float heightPixels = ViewUtils.convertDpToPixel(100);

        final RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, (int) heightPixels);
        lp.addRule(RelativeLayout.ALIGN_PARENT_TOP);

        final ContentView view = new ContentView(parent.getContext(), params.screenInstanceId, params.navigationParams);
        view.setLayoutParams(lp);
        view.setVisibility(View.INVISIBLE);
        return view;
    }

    protected void onSlidingOverlayShown(ContentView view) {
        visibilityState = VisibilityState.Shown;
        if (listener != null) {
            listener.onSlidingOverlayShown();
        }
    }

    protected void onSlidingOverlayEnd(ContentView view) {
        visibilityState = VisibilityState.Hidden;
        view.unmountReactView();
        parent.removeView(view);

        if (listener != null) {
            listener.onSlidingOverlayGone();
        }
    }
}
