package uni.university.shoppinglist.touch

interface TouchHelperAdapterInterface {
    fun onItemDismiss(position: Int)
    fun onItemMove(fromPosition: Int, toPosition: Int)
}
