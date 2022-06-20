package uni.university.shoppinglist.recycler_view

import android.view.View
import android.widget.CheckBox
import android.widget.Switch
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import de.hdodenhof.circleimageview.CircleImageView
import uni.university.shoppinglist.R

class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val mItemImage : CircleImageView = itemView.findViewById(R.id.itemImage)
    val mItemName: TextView = itemView.findViewById(R.id.itemText)
    val mItemPrice: TextView = itemView.findViewById(R.id.itemPrice)
    val mItemDescription: TextView = itemView.findViewById(R.id.itemDescription)
    val mItemPurchased :Switch= itemView.findViewById(R.id.itemPurchased)
}
