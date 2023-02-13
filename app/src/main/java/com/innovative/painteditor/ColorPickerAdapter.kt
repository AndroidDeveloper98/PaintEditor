package com.innovative.painteditor

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView

/**
 * Created by Ahmed Adel on 5/8/17.
 */
class ColorPickerAdapter internal constructor(
    private var context: Context,
    colorPickerColors: List<Int>
) : RecyclerView.Adapter<ColorPickerAdapter.ViewHolder>() {
    private var inflater: LayoutInflater
    private val colorPickerColors: List<Int>
    private lateinit var onColorPickerClickListener: OnColorPickerClickListener
    private var selectedColor = 0

    internal constructor(context: Context) : this(context, getDefaultColors(context)) {
        this.context = context
        inflater = LayoutInflater.from(context)
    }

    fun setColor(position: Int) {
        selectedColor = position
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = inflater.inflate(R.layout.color_picker_item_list, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.colorPickerView.setCardBackgroundColor(colorPickerColors[position])
        holder.ivCheck.setColorFilter(
            ContextCompat.getColor(
                context,
                R.color.white
            )
        )
        if (selectedColor == position){
            holder.ivCheck.visibility = View.VISIBLE
            if (position == 1) {
                holder.ivCheck.setColorFilter(
                    ContextCompat.getColor(
                        context,
                        R.color.black
                    )
                )
            } else {
                holder.ivCheck.setColorFilter(
                    ContextCompat.getColor(
                        context,
                        R.color.white
                    )
                )
            }
        } else {
            holder.ivCheck.visibility = View.GONE
        }
    }

    override fun getItemCount(): Int {
        return colorPickerColors.size
    }

    fun setOnColorPickerClickListener(onColorPickerClickListener: OnColorPickerClickListener) {
        this.onColorPickerClickListener = onColorPickerClickListener
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var colorPickerView: CardView = itemView.findViewById(R.id.color_picker_view)
        var ivCheck: ImageView = itemView.findViewById(R.id.ivCheck)

        init {
            itemView.setOnClickListener {
                onColorPickerClickListener.onColorPickerClickListener(
                    colorPickerColors[adapterPosition],adapterPosition
                )
            }
        }
    }

    interface OnColorPickerClickListener {
        fun onColorPickerClickListener(colorCode: Int,position: Int)
    }

    companion object {
        fun getDefaultColors(context: Context): List<Int> {
            val colorPickerColors = ArrayList<Int>()
            colorPickerColors.add(ContextCompat.getColor((context), R.color.black))
            colorPickerColors.add(ContextCompat.getColor((context), R.color.white))
            colorPickerColors.add(ContextCompat.getColor((context), R.color.blue_color_picker))
            colorPickerColors.add(ContextCompat.getColor((context), R.color.brown_color_picker))
            colorPickerColors.add(ContextCompat.getColor((context), R.color.green_color_picker))
            colorPickerColors.add(ContextCompat.getColor((context), R.color.orange_color_picker))
            colorPickerColors.add(ContextCompat.getColor((context), R.color.red_color_picker))
            colorPickerColors.add(
                ContextCompat.getColor(
                    (context),
                    R.color.red_orange_color_picker
                )
            )
            colorPickerColors.add(
                ContextCompat.getColor(
                    (context),
                    R.color.sky_blue_color_picker
                )
            )
            colorPickerColors.add(ContextCompat.getColor((context), R.color.violet_color_picker))
            colorPickerColors.add(ContextCompat.getColor((context), R.color.yellow_color_picker))
            colorPickerColors.add(
                ContextCompat.getColor(
                    (context),
                    R.color.yellow_green_color_picker
                )
            )
            return colorPickerColors
        }
    }

    init {
        inflater = LayoutInflater.from(context)
        this.colorPickerColors = colorPickerColors
    }
}