package com.example.mylist

import android.app.Application
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType

class ItemViewModel(application: Application) : AndroidViewModel(application) {
    private var repo:ItemRepository
    var allItems: LiveData<List<Item>>

    init {
        repo= ItemRepository(ItemDatabase.getDatabase(application).itemDao())
        allItems=repo.allItems
    }

    fun insert(item:Item)=viewModelScope.launch {
        repo.insert(item)
    }
    fun delete(item:Item)=viewModelScope.launch {
        repo.delete(item)
    }
    fun updates(item:Item)=viewModelScope.launch {
        repo.update(item)
    }
}


//LiveData/Flow: Used for reactive programming, observing changes in data.
//State: Used within Compose to hold data and trigger recompositions when the data changes.
// Compose relies on State to know when to re-render parts of the UI. State ensures that changes in data automatically update the UI.
@Composable
fun screen(viewModel: ItemViewModel) {
    val itemList by viewModel.allItems.observeAsState(emptyList())
    var isAddNewItem:Boolean by remember{ mutableStateOf(false)}
    val context: Context = LocalContext.current
    if(isAddNewItem)
    {
        addNewItem(onComplete=fun(item:Item)
        {
            isAddNewItem=false

            if(item.quantity!="" && item.name!="")
                viewModel.insert(item)
        },
            context)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(Modifier.fillMaxWidth())
        {
            IconButton(onClick = {

            }) {
                Icon(Icons.Default.ShoppingCart, contentDescription = "")
            }

            Button(onClick = {
                isAddNewItem = true
            },
                Modifier.padding(start=103.dp)) {
                Text(text = "Add")
            }
        }

        LazyColumn(modifier = Modifier
            .fillMaxSize()
            .padding(10.dp))
        {
            items(itemList){item->
                if(item.isEditing)
                {
                    itemEditingView(item = item,viewModel)
                }
                else
                {
                    Visible(item = item, viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun itemEditingView(item: Item,viewModel: ItemViewModel)
{
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(70.dp)
            .padding(5.dp)
        ,horizontalArrangement = Arrangement.SpaceBetween
    )
    {
        var tempName by remember { mutableStateOf(item.name) }
        var tempCount by remember{ mutableStateOf(item.quantity) }
        Column(modifier= Modifier
            .fillMaxHeight()
            .weight(1f)
            .background(color = Color.LightGray, RoundedCornerShape(20)), verticalArrangement = Arrangement.SpaceBetween)
        {
            BasicTextField(value = tempName
                ,onValueChange = { tempName = it }
                ,modifier= Modifier
                    .wrapContentSize()
                    .padding(start = 8.dp, top = 2.dp)
                    .weight(1f)
                ,textStyle = TextStyle(color = Color.Black)
                , singleLine = true
                , keyboardOptions = KeyboardOptions(keyboardType=KeyboardType.Email, autoCorrect = true)
                //Basically when we click on a text field a keyboard automatically gets opened, we can decide the type of
                // keyboard we want to open, example- text keyboard, numeric keyboard etc.
            )
            BasicTextField(value = tempCount
                ,onValueChange = { tempCount = it }
                ,modifier= Modifier
                    .wrapContentSize()
                    .padding(start = 8.dp, bottom = 2.dp)
                    .weight(1f)
                ,textStyle = TextStyle(color = Color.Black)
                , singleLine = true
            )

        }
        Button(onClick = {
            /*
            item.isEditing=false
            item.quantity=tempCount
            item.name=tempName
            viewModel.updates(item)

            we SHOULD not write like this as it do not assure that the UI gets triggered/notified of the changes made to the list, and hence sometimes do not refreshes the screen
            instead we should prefer using copy() function as in the following line
            */

            viewModel.updates(item.copy(name=tempName, quantity = tempCount, isEditing = false)) }
            ,modifier=Modifier.padding(top=4.dp,start=4.dp)) {
            Text(text = "Save")
        }
        Button(onClick = {
            viewModel.updates(item.copy(name=tempName, quantity = tempCount, isEditing = false)) }
            ,modifier=Modifier.padding(top=4.dp,start=4.dp)) {
            Text(text = "Cancel")
        }
    }
}

@Composable
fun Visible(item:Item,viewModel:ItemViewModel) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(70.dp)
            .padding(5.dp)
            .border(
                BorderStroke(width = 1.dp, color = Color.Black),
                shape = RoundedCornerShape(25)
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    )
    {
        LazyRow(
            modifier = Modifier
                .width(150.dp)
                .padding(start = 10.dp, end = 20.dp)
        )
        {
            item { Text(text = item.name) }
            //Text(text = temp.name, modifier = Modifier.padding(start = 10.dp), maxLines = 1)
        }
        Text(text = "Qty : ${item.quantity}", maxLines = 1, modifier = Modifier.width(110.dp))
        Row(modifier = Modifier.fillMaxHeight(), verticalAlignment = Alignment.Bottom)
        {
            IconButton(onClick = {
                /*
                item.isEditing=true
                viewModel.updates(item)

                these types of updates/lines should be avoided
                */
                viewModel.updates(item.copy(isEditing = true))
            }) {
                Icon(Icons.Default.Create, contentDescription = "")
            }

            IconButton(onClick = {
                viewModel.delete(item)
            }) {
                Icon(Icons.Default.Delete, contentDescription = "")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun addNewItem(onComplete:(item:Item)->Unit,context: Context) {
    var strName:String by remember{ mutableStateOf("") }
    var strCount:String by remember{ mutableStateOf("") }
    AlertDialog(
        onDismissRequest = { Toast.makeText(context, "Invalid", Toast.LENGTH_SHORT).show() },
        confirmButton = { /*TODO*/ },
        text={
            Column {
                OutlinedTextField(value =strName , onValueChange ={strName=it} ,label={Text(text="Name")},modifier = Modifier.padding(8.dp), singleLine = true)
                OutlinedTextField(
                    value =strCount ,
                    onValueChange ={strCount=it} ,
                    label={Text(text="Quantity")},
                    modifier = Modifier.padding(8.dp),
                    singleLine = true,
//                    colors=TextFieldDefaults.outlinedTextFieldColors(
//                         using this we can modify colour to every action like on select text colour, container colour etc.
//                    )
                )
                Row (modifier= Modifier
                    .fillMaxWidth()
                    .padding(start = 15.dp, end = 15.dp),horizontalArrangement = Arrangement.SpaceBetween){
                    Button(onClick =
                    {   if(strCount=="" || strName=="") {
                        Toast.makeText(context, "Invalid", Toast.LENGTH_SHORT).show()
                    }
                    else{
                       onComplete(Item(name=strName, quantity = strCount))
                    }
                    })
                    {
                        Text(text = "Add")
                    }

                    Button(onClick =
                    {
                        onComplete(Item(name=strName, quantity = strCount))
                    }) {
                        Text(text = "Cancel")
                    }
                }
            }
        })
}