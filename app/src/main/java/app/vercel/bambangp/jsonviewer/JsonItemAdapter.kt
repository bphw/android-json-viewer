package app.vercel.bambangp.jsonviewer

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import java.util.Locale

class JsonItemAdapter(
    private var items: List<JsonItem>,
    private val context: Context
) : RecyclerView.Adapter<JsonItemAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val key: TextView = view.findViewById(R.id.tvKey)
        val value: TextView = view.findViewById(R.id.tvValue)
        val expandIcon: ImageView = view.findViewById(R.id.ivExpand)
        val nestedItems: LinearLayout = view.findViewById(R.id.nestedItems)
    }

    enum class JsonType { OBJECT, ARRAY, STRING, NUMBER, BOOLEAN, NULL }

    data class JsonItem(
        val key: String,
        val value: String?,
        val type: JsonType,
        val children: List<JsonItem> = emptyList(),
        var isExpanded: Boolean = false,
        val depth: Int = 0
    )

    companion object {
        fun parseJson(json: String): List<JsonItem> {
            return try {
                val jsonElement = JsonParser.parseString(json)
                // Handle root element properly
                when {
                    jsonElement.isJsonObject -> {
                        val obj = jsonElement.asJsonObject
                        obj.entrySet().map { (k, v) ->
                            val children = if (!v.isJsonPrimitive) parseJsonElement(null, v, 1) else emptyList()
                            JsonItem(
                                key = k,
                                value = if (v.isJsonPrimitive) v.asString else null,
                                type = if (v.isJsonPrimitive) getPrimitiveType(v) else getComplexType(v),
                                children = children,
                                depth = 0
                            )
                        }
                    }
                    jsonElement.isJsonArray -> {
                        jsonElement.asJsonArray.mapIndexed { index, item ->
                            val children = if (!item.isJsonPrimitive) parseJsonElement(null, item, 1) else emptyList()
                            JsonItem(
                                key = "[$index]",
                                value = if (item.isJsonPrimitive) item.asString else null,
                                type = if (item.isJsonPrimitive) getPrimitiveType(item) else getComplexType(item),
                                children = children,
                                depth = 0
                            )
                        }
                    }
                    else -> listOf(
                        JsonItem(
                            key = "root",
                            value = jsonElement.asString,
                            type = getPrimitiveType(jsonElement),
                            depth = 0
                        )
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
                emptyList()
            }
        }

        private fun parseJsonElement(key: String?, element: JsonElement, depth: Int = 0): List<JsonItem> {
            return when {
                element.isJsonObject -> {
                    val obj = element.asJsonObject
                    obj.entrySet().map { (k, v) ->
                        JsonItem(
                            key = k,
                            value = if (v.isJsonPrimitive) v.asString else null,
                            type = if (v.isJsonPrimitive) getPrimitiveType(v) else getComplexType(v),
                            children = if (!v.isJsonPrimitive) parseJsonElement(null, v, depth + 1) else emptyList(),
                            depth = depth
                        )
                    }
                }
                element.isJsonArray -> {
                    element.asJsonArray.mapIndexed { index, item ->
                        JsonItem(
                            key = "[$index]",
                            value = if (item.isJsonPrimitive) item.asString else null,
                            type = if (item.isJsonPrimitive) getPrimitiveType(item) else getComplexType(item),
                            children = if (!item.isJsonPrimitive) parseJsonElement(null, item, depth + 1) else emptyList(),
                            depth = depth
                        )
                    }
                }
                else -> listOf(
                    JsonItem(
                        key = key ?: "",
                        value = element.asString,
                        type = getPrimitiveType(element),
                        depth = depth
                    )
                )
            }
        }

        private fun getPrimitiveType(element: JsonElement): JsonType {
            return when {
                element.isJsonNull -> JsonType.NULL
                element.asJsonPrimitive.isBoolean -> JsonType.BOOLEAN
                element.asJsonPrimitive.isNumber -> JsonType.NUMBER
                else -> JsonType.STRING
            }
        }

        private fun getComplexType(element: JsonElement): JsonType {
            return when {
                element.isJsonObject -> JsonType.OBJECT
                element.isJsonArray -> JsonType.ARRAY
                else -> JsonType.STRING
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_json, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]

        // Set key and value
        holder.key.text = item.key
        
        // Display appropriate value based on type
        when (item.type) {
            JsonType.OBJECT -> {
                holder.value.text = "{...}"
                holder.value.visibility = View.VISIBLE
            }
            JsonType.ARRAY -> {
                holder.value.text = "[...]"
                holder.value.visibility = View.VISIBLE
            }
            else -> {
                holder.value.text = item.value ?: ""
                holder.value.visibility = if (item.value.isNullOrEmpty()) View.GONE else View.VISIBLE
            }
        }

        // Set text color based on type
        val colorRes = when(item.type) {
            JsonType.STRING -> R.color.json_string
            JsonType.NUMBER -> R.color.json_number
            JsonType.BOOLEAN -> R.color.json_boolean
            JsonType.NULL -> R.color.json_null
            JsonType.OBJECT -> R.color.json_key
            JsonType.ARRAY -> R.color.json_key
            else -> android.R.color.black
        }
        holder.value.setTextColor(ContextCompat.getColor(context, colorRes))

        // Handle expandable items
        if (item.children.isNotEmpty()) {
            holder.expandIcon.visibility = View.VISIBLE
            holder.expandIcon.setImageResource(
                if (item.isExpanded) R.drawable.ic_expand_less
                else R.drawable.ic_expand_more
            )

            holder.itemView.setOnClickListener {
                item.isExpanded = !item.isExpanded
                notifyItemChanged(position)
            }

            toggleNestedItemsRecursive(holder.nestedItems, item)
        } else {
            holder.expandIcon.visibility = View.GONE
            holder.nestedItems.visibility = View.GONE
        }

        // Set long click listener for copy/paste
        holder.itemView.setOnLongClickListener {
            showCopyMenu(holder.itemView, item)
            true
        }

        // Apply indentation based on depth
        holder.itemView.setPadding(
            context.resources.getDimensionPixelSize(R.dimen.json_item_indent) * item.depth,
            holder.itemView.paddingTop,
            holder.itemView.paddingEnd,
            holder.itemView.paddingBottom
        )
    }

    private fun toggleNestedItems(layout: LinearLayout, item: JsonItem) {
        if (item.isExpanded) {
            layout.visibility = View.VISIBLE
            if (layout.childCount == 0) {
                bindNestedItems(layout, item.children)
            }
        } else {
            layout.visibility = View.GONE
        }
    }

    private fun toggleNestedItemsRecursive(layout: LinearLayout, item: JsonItem) {
        if (item.isExpanded) {
            layout.visibility = View.VISIBLE
            // Always refresh the nested items to ensure proper state
            layout.removeAllViews()
            item.children.forEach { child ->
                val childView = LayoutInflater.from(layout.context)
                    .inflate(R.layout.item_json, layout, false)
                bindViewToLayoutRecursive(childView, child)
                layout.addView(childView)
            }
        } else {
            layout.visibility = View.GONE
        }
    }

    private fun bindViewToLayoutRecursive(view: View, item: JsonItem) {
        val holder = ViewHolder(view)
        
        // Set key and value
        holder.key.text = item.key
        
        // Display appropriate value based on type
        when (item.type) {
            JsonType.OBJECT -> {
                holder.value.text = "{...}"
                holder.value.visibility = View.VISIBLE
            }
            JsonType.ARRAY -> {
                holder.value.text = "[...]"
                holder.value.visibility = View.VISIBLE
            }
            else -> {
                holder.value.text = item.value ?: ""
                holder.value.visibility = if (item.value.isNullOrEmpty()) View.GONE else View.VISIBLE
            }
        }

        // Set text color based on type
        val colorRes = when(item.type) {
            JsonType.STRING -> R.color.json_string
            JsonType.NUMBER -> R.color.json_number
            JsonType.BOOLEAN -> R.color.json_boolean
            JsonType.NULL -> R.color.json_null
            JsonType.OBJECT -> R.color.json_key
            JsonType.ARRAY -> R.color.json_key
            else -> android.R.color.black
        }
        holder.value.setTextColor(ContextCompat.getColor(context, colorRes))

        // Handle expandable items recursively
        if (item.children.isNotEmpty()) {
            holder.expandIcon.visibility = View.VISIBLE
            holder.expandIcon.setImageResource(
                if (item.isExpanded) R.drawable.ic_expand_less
                else R.drawable.ic_expand_more
            )

            holder.itemView.setOnClickListener {
                item.isExpanded = !item.isExpanded
                // Update the expand icon
                holder.expandIcon.setImageResource(
                    if (item.isExpanded) R.drawable.ic_expand_less
                    else R.drawable.ic_expand_more
                )
                toggleNestedItemsRecursive(holder.nestedItems, item)
            }

            // Initialize the nested items state
            toggleNestedItemsRecursive(holder.nestedItems, item)
        } else {
            holder.expandIcon.visibility = View.GONE
            holder.nestedItems.visibility = View.GONE
        }

        // Set long click listener for copy/paste
        holder.itemView.setOnLongClickListener {
            showCopyMenu(holder.itemView, item)
            true
        }

        // Apply indentation based on depth
        holder.itemView.setPadding(
            context.resources.getDimensionPixelSize(R.dimen.json_item_indent) * item.depth,
            holder.itemView.paddingTop,
            holder.itemView.paddingEnd,
            holder.itemView.paddingBottom
        )
    }

    private fun bindNestedItems(layout: LinearLayout, children: List<JsonItem>) {
        layout.removeAllViews()
        children.forEach { child ->
            val view = LayoutInflater.from(layout.context)
                .inflate(R.layout.item_json, layout, false)
            bindViewToLayout(view, child)
            layout.addView(view)
        }
    }

    private fun bindViewToLayout(view: View, item: JsonItem) {
        val holder = ViewHolder(view)
        
        // Set key and value
        holder.key.text = item.key
        
        // Display appropriate value based on type
        when (item.type) {
            JsonType.OBJECT -> {
                holder.value.text = "{...}"
                holder.value.visibility = View.VISIBLE
            }
            JsonType.ARRAY -> {
                holder.value.text = "[...]"
                holder.value.visibility = View.VISIBLE
            }
            else -> {
                holder.value.text = item.value ?: ""
                holder.value.visibility = if (item.value.isNullOrEmpty()) View.GONE else View.VISIBLE
            }
        }

        // Set text color based on type
        val colorRes = when(item.type) {
            JsonType.STRING -> R.color.json_string
            JsonType.NUMBER -> R.color.json_number
            JsonType.BOOLEAN -> R.color.json_boolean
            JsonType.NULL -> R.color.json_null
            JsonType.OBJECT -> R.color.json_key
            JsonType.ARRAY -> R.color.json_key
            else -> android.R.color.black
        }
        holder.value.setTextColor(ContextCompat.getColor(context, colorRes))

        // Handle expandable items
        if (item.children.isNotEmpty()) {
            holder.expandIcon.visibility = View.VISIBLE
            holder.expandIcon.setImageResource(
                if (item.isExpanded) R.drawable.ic_expand_less
                else R.drawable.ic_expand_more
            )

            holder.itemView.setOnClickListener {
                item.isExpanded = !item.isExpanded
                // Update the expand icon
                holder.expandIcon.setImageResource(
                    if (item.isExpanded) R.drawable.ic_expand_less
                    else R.drawable.ic_expand_more
                )
                toggleNestedItemsRecursive(holder.nestedItems, item)
            }

            // Initialize the nested items state
            toggleNestedItemsRecursive(holder.nestedItems, item)
        } else {
            holder.expandIcon.visibility = View.GONE
            holder.nestedItems.visibility = View.GONE
        }

        // Set long click listener for copy/paste
        holder.itemView.setOnLongClickListener {
            showCopyMenu(holder.itemView, item)
            true
        }

        // Apply indentation based on depth
        holder.itemView.setPadding(
            context.resources.getDimensionPixelSize(R.dimen.json_item_indent) * item.depth,
            holder.itemView.paddingTop,
            holder.itemView.paddingEnd,
            holder.itemView.paddingBottom
        )
    }

    fun bindView(view: View, item: JsonItem) {
        val holder = ViewHolder(view)
        onBindViewHolder(holder, items.indexOf(item))
    }

    private fun showCopyMenu(view: View, item: JsonItem) {
        val popup = PopupMenu(context, view)
        popup.menuInflater.inflate(R.menu.copy_menu, popup.menu)

        // Disable irrelevant options
        popup.menu.findItem(R.id.copy_key).isVisible = item.key.isNotEmpty()
        popup.menu.findItem(R.id.copy_value).isVisible = !item.value.isNullOrEmpty()

        popup.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.copy_key -> {
                    copyToClipboard(item.key, "Key copied")
                    true
                }
                R.id.copy_value -> {
                    copyToClipboard(item.value ?: "", "Value copied")
                    true
                }
                R.id.copy_path -> {
                    copyToClipboard(getFullPath(item), "Path copied")
                    true
                }
                else -> false
            }
        }
        popup.show()
    }

    private fun copyToClipboard(text: String, toastMessage: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("JSON Viewer", text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, toastMessage, Toast.LENGTH_SHORT).show()
    }

    private fun getFullPath(item: JsonItem): String {
        return buildString {
            append(item.key)
            if (!item.value.isNullOrEmpty()) {
                append(": ").append(item.value)
            }
        }
    }

    fun updateItems(newItems: List<JsonItem>) {
        items = newItems
        notifyDataSetChanged()
    }

    override fun getItemCount() = items.size
}