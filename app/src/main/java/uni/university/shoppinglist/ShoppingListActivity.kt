package uni.university.shoppinglist

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import uni.university.shoppinglist.data.ItemType
import uni.university.shoppinglist.data.ListItem
import uni.university.shoppinglist.recycler_view.RecyclerViewAdapter
import androidx.recyclerview.widget.ItemTouchHelper
import uni.university.shoppinglist.touch.TouchHelperCallback
import java.util.*


class ShoppingListActivity : AppCompatActivity() {
    private var recyclerView: RecyclerView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_shopping_list)

        // set toolbar
        setSupportActionBar(findViewById<Toolbar>(R.id.toolbar))

        // set float button listeners
        findViewById<FloatingActionButton>(R.id.fabAdd).setOnClickListener {
            DialogShow()
        }

        findViewById<FloatingActionButton>(R.id.fabDelAll).setOnClickListener {
            RemoveAll()
        }

        // init recycler view
        val recyclerViewAdapter = RecyclerViewAdapter(this)
        recyclerView = findViewById(R.id.recyclerView)
        recyclerView!!.adapter = recyclerViewAdapter
        recyclerView!!.layoutManager = LinearLayoutManager(this)

        // adding touch support
        val callback = TouchHelperCallback(recyclerViewAdapter)
        ItemTouchHelper(callback).attachToRecyclerView(recyclerView)
    }

    private fun DialogShow() {
        val dialogView: View = LayoutInflater.from(this).inflate(R.layout.dialog_add, null)

        val builder = AlertDialog.Builder(this).let {
            it.setTitle("New Item")
            it.setView(dialogView)
        }

        val CatSpin = dialogView.findViewById<Spinner>(R.id.spinnerItemCategory)
        var category: ItemType = ItemType.FOOD

        CatSpin.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                category = when (CatSpin.selectedItem) {
                    "Food" -> ItemType.FOOD
                    "Cleaning" -> ItemType.CLEANING
                    "Entertainment" -> ItemType.ENTERTAINMENT
                    else -> ItemType.FOOD
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                category = ItemType.FOOD
            }
        }

        builder.setPositiveButton("Add") { _, _ ->
            val name: String = dialogView.findViewById<EditText>(R.id.editItemName).text.toString()
            val description: String = dialogView.findViewById<EditText>(R.id.editItemDescription).text.toString()

            val priceStr: String = dialogView.findViewById<EditText>(R.id.editItemPrice).text.toString()
            val priceInt: Int = if (priceStr.isNotEmpty()) priceStr.toInt() else 0

            (recyclerView!!.adapter as RecyclerViewAdapter)
                .addItem(ListItem(category, name, priceInt, description, UUID.randomUUID().toString()))

            recyclerView!!.scrollToPosition(0)
        }

        builder.setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
        builder.show()
    }

    private fun RemoveAll() {
        val dialogView: View = LayoutInflater.from(this).inflate(R.layout.dialog_del, null)
        val builder = AlertDialog.Builder(this)
        builder.setView(dialogView)

        builder.setPositiveButton("Delete everything") { _, _ ->
            (recyclerView!!.adapter as RecyclerViewAdapter).deleteAll(recyclerView!!)
        }

        builder.setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }

        builder.show()
    }

    fun Modify(position: Int, item: ListItem) {
        val dialogView: View = LayoutInflater.from(this).inflate(R.layout.dialog_add, null)
        val builder = AlertDialog.Builder(this)
        builder.setView(dialogView)

        val SeeTitle = dialogView.findViewById<EditText>(R.id.editItemName)
        val SeeDetail = dialogView.findViewById<EditText>(R.id.editItemDescription)
        val SeePrice = dialogView.findViewById<EditText>(R.id.editItemPrice)
        val SeeCategory = dialogView.findViewById<Spinner>(R.id.spinnerItemCategory)

        SeeTitle.setText(item.mName)
        SeePrice.setText(item.mPrice.toString())
        SeeDetail.setText(item.mDescription)
        SeeCategory.setSelection(item.getCategoryEnum().ordinal)

        var category : String? = null

        SeeCategory.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                 category = SeeCategory.selectedItem.toString()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                category = "Food"
            }
        }

        builder.setPositiveButton("Save") { _, _ ->
            (recyclerView!!.adapter as RecyclerViewAdapter).updateItem(
                item.mId,
                category!!,
                SeeTitle.text.toString(),
                if (SeePrice.text.toString().isNotEmpty()) SeePrice.text.toString().toInt() else 0,
                SeeDetail.text.toString(),
                position
            )

            recyclerView!!.scrollToPosition(0)
        }

        builder.setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }

        builder.show()
    }

    override fun onDestroy() {
        (recyclerView!!.adapter as RecyclerViewAdapter).close()
        super.onDestroy()
    }
}
