package uni.university.shoppinglist.data

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import java.lang.Exception

open class ListItem(
    type: ItemType = ItemType.FOOD,
    name: String = "",
    price: Int = -1,
    description: String = "",
    id: String = ""
) : RealmObject() {

    @PrimaryKey
    var id: String? = null

    var mId: String = id
    var mImage: String? = null
    var mName: String = name
    var mPrice: Int = price
    var mDescription: String = description
    var mIsPurchased: Boolean = false
    var mOrder: Int = 0
    var mCategory : String = getStringFromEnum(type)

    init {
        mImage = "ic_" + getStringFromEnum(type).toLowerCase()
    }

    private fun getStringFromEnum(type: ItemType) : String {
        return when (type) {
            ItemType.FOOD -> "Food"
            ItemType.CLEANING -> "Cleaning"
            ItemType.ENTERTAINMENT -> "Entertainment"
        }
    }

    fun getCategoryEnum() : ItemType {
        return when (mCategory) {
            "Food" -> ItemType.FOOD
            "Cleaning" -> ItemType.CLEANING
            "Entertainment" -> ItemType.ENTERTAINMENT
            else -> throw Exception("ListItem: Cannot get category ENUM")
        }
    }

    fun updateImage(typeStr: String) {
        mImage = "ic_" + typeStr.toLowerCase()
    }
}
