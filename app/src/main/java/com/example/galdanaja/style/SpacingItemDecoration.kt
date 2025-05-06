package com.example.galdanaja.style

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class HorizontalMarginItemDecoration(private val horizontalMarginInPx: Int) :
    RecyclerView.ItemDecoration() {

    override fun getItemOffsets(
        outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State
    ) {
        val position = parent.getChildAdapterPosition(view)

        if (position %2 == 1) {
            // Hanya item kedua yang dapat margin kiri dan kanan
            outRect.left = horizontalMarginInPx
            outRect.right = horizontalMarginInPx
        } else {
            // Item lain tidak dapat margin
            outRect.left = 0
            outRect.right = 0
        }
    }
}



