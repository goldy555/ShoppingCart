package uni.university.shoppinglist.recycler_view

import  android.content.Context
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import io.realm.Realm
import io.realm.RealmResults
import io.realm.Sort
import uni.university.shoppinglist.R
import uni.university.shoppinglist.ShoppingListActivity
import uni.university.shoppinglist.data.ListItem
import uni.university.shoppinglist.touch.TouchHelperAdapterInterface
import kotlin.collections.ArrayList
import android.widget.TextView


class RecyclerViewAdapter(context: Context) : RecyclerView.Adapter<ViewHolder>(), TouchHelperAdapterInterface {

    private val mItems = ArrayList<ListItem>()
    private val mContext = context
    private val realm = Realm.getDefaultInstance()
    private var currentSum = 0
    private var totalSum = 0

    init {
        val prefs = mContext.applicationContext.getSharedPreferences("totalSumPrefs", Context.MODE_PRIVATE)

        realm.where(ListItem::class.java).sort("mOrder", Sort.ASCENDING).findAll().forEach {
            currentSum += it.mPrice
            mItems.add(it)
        }

        totalSum = prefs.getInt("totalSum", 0)

        val listActivity = mContext as ShoppingListActivity

        listActivity.findViewById<TextView>(R.id.totalSum).text =
            listActivity.getString(R.string.total_spent, totalSum.toString())

        updateCurrentSum(currentSum)
    }

    private fun updateCurrentSum(newSum: Int) {
        val listActivity = mContext as ShoppingListActivity

        currentSum = if (newSum > 0) newSum else 0

        listActivity.findViewById<TextView>(R.id.currentSum).text =
            listActivity.getString(R.string.total_in_list, currentSum.toString())
    }

    private fun updateTotalSum(newSum: Int) {
        totalSum = if (newSum > 0) newSum else totalSum

        val prefs = mContext.applicationContext.getSharedPreferences("totalSumPrefs", Context.MODE_PRIVATE)

        val listActivity = mContext as ShoppingListActivity

        listActivity.findViewById<TextView>(R.id.totalSum).text =
            listActivity.getString(R.string.total_spent, totalSum.toString())

        val editor = prefs.edit()
        editor.putInt("totalSum", totalSum)
        editor.apply()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.layout_list_item, parent, false)
        )
    }

    override fun getItemCount(): Int {
        return mItems.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val drawableId: Int =
            mContext.resources.getIdentifier(mItems[position].mImage, "drawable", mContext.packageName)

        holder.mItemImage.setImageResource(drawableId)
        holder.mItemName.text = mItems[position].mName
        holder.mItemPrice.text = mContext.getString(R.string.list_item_price, mItems[position].mPrice.toString())
        holder.mItemDescription.text = mItems[position].mDescription
        holder.mItemPurchased.isChecked = mItems[position].mIsPurchased

        holder.mItemPurchased.setOnClickListener {
            realm.beginTransaction()
            val item = realm.where(ListItem::class.java).equalTo("id", mItems[position].mId).findFirst()
            item!!.mIsPurchased = !mItems[position].mIsPurchased
            realm.commitTransaction()

            mItems[position] = item
        }

        holder.itemView.setOnClickListener {
            (mContext as ShoppingListActivity).Modify(position, mItems[position])
        }

        setBottomPadding(holder.itemView, position, 80)
    }

    override fun onItemDismiss(position: Int) {
        updateCurrentSum(currentSum - mItems[position].mPrice)

        realm.beginTransaction()
        val item: ListItem? = realm.where(ListItem::class.java).equalTo("id", mItems[position].mId).findFirst()
        item!!.deleteFromRealm()
        realm.commitTransaction()

        mItems.removeAt(position)

        notifyItemRemoved(position)
        notifyItemRangeChanged(mItems.size - 2, 2)
    }

    override fun onItemMove(fromPosition: Int, toPosition: Int) {
        val results: RealmResults<ListItem> = realm.where(ListItem::class.java)
            .sort("mOrder", Sort.ASCENDING)
            .findAll()

        realm.beginTransaction()

        if (fromPosition < toPosition) {
            for (i in fromPosition until toPosition) {
                results[i]!!.mOrder = results[i + 1]!!.mOrder.also { results[i + 1]!!.mOrder = results[i]!!.mOrder }
                mItems[i] = mItems[i + 1].also { mItems[i + 1] = mItems[i] }
            }
        } else {
            for (i in fromPosition downTo toPosition + 1) {
                results[i]!!.mOrder = results[i - 1]!!.mOrder.also { results[i - 1]!!.mOrder = results[i]!!.mOrder }
                mItems[i] = mItems[i - 1].also { mItems[i - 1] = mItems[i] }
            }
        }

        realm.commitTransaction()

        notifyItemMoved(fromPosition, toPosition)
    }

    private fun setBottomPadding(view: View, position: Int, padding: Int) {
        val scale = mContext.resources.displayMetrics.density
        val dpAsPixels = (padding * scale + 0.5f).toInt()
        val defaultPadding = 60

        view.setPadding(
            defaultPadding,
            defaultPadding,
            defaultPadding,
            if (position == mItems.size - 1 && mItems.size >= 5) dpAsPixels else defaultPadding
        )
    }

    fun addItem(item: ListItem) {
        updateCurrentSum(currentSum + item.mPrice)
        updateTotalSum(totalSum + item.mPrice)

        val results: RealmResults<ListItem> = realm.where(ListItem::class.java)
            .sort("mOrder", Sort.ASCENDING)
            .findAll()

        realm.beginTransaction()

        results.forEach { it.mOrder++ }

        val newItem = realm.createObject(ListItem::class.java, item.mId)
        newItem.mId = item.mId
        newItem.mName = item.mName
        newItem.mPrice = item.mPrice
        newItem.mDescription = item.mDescription
        newItem.mImage = item.mImage
        newItem.mIsPurchased = item.mIsPurchased
        newItem.mOrder = item.mOrder
        mItems.add(0, item)

        realm.commitTransaction()

        notifyItemInserted(0)
        notifyItemRangeChanged(mItems.size - 2, 2)
    }

    fun deleteAll(recyclerView: RecyclerView) {
        realm.beginTransaction()
        realm.delete(ListItem::class.java)
        realm.commitTransaction()

        var stop = false
        val handler = Handler()

        val duration : Long = 400

        val runnable = object : Runnable {
            override fun run() {
                if (mItems.size == 0) {
                    stop = true
                }

                if (!stop) {
                    mItems.removeAt(0)
                    notifyItemRemoved(0)
                }

                handler.postDelayed(this, duration)
            }
        }

        updateCurrentSum(0)

        (mContext as ShoppingListActivity).runOnUiThread(runnable)
    }

    fun updateItem(id: String, category: String, name: String, price: Int, description: String, position: Int) {
        updateCurrentSum(currentSum - mItems[position].mPrice + price)
        updateTotalSum(totalSum - mItems[position].mPrice + price)

        val item: ListItem = realm.where(ListItem::class.java).equalTo("id", id).findFirst()!!
        realm.beginTransaction()
        item.mCategory = category
        item.mName = name
        item.mPrice = price
        item.mDescription = description
        item.updateImage(category)
        realm.commitTransaction()

        mItems[position] = item
        notifyItemChanged(position)
    }

    fun close() {
        realm!!.close()
    }
}
