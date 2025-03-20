package com.example.receivecontentexample

import android.graphics.BitmapFactory
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.content.MediaType
import androidx.compose.foundation.content.ReceiveContentListener
import androidx.compose.foundation.content.consume
import androidx.compose.foundation.content.contentReceiver
import androidx.compose.foundation.content.hasMediaType
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import com.example.receivecontentexample.ui.theme.ReceiveContentExampleTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ReceiveContentExampleTheme {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    Spacer(modifier = Modifier.height(32.dp))

                    Text(
                        "TextField sin contentReceiver",
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    TextFieldWithoutContentReceiver()

                    Text(
                        "Ejemplo de TextField con contentReceiver",
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    TextFieldWithContentReceiver()
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        "Ejemplo de área de drop para imágenes",
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    ImageDropArea()
                }
            }
        }
    }
}

@Composable
fun TextFieldWithoutContentReceiver() {
    var text by remember { mutableStateOf("") }

    // Este TextField no intercepta contenido; solo procesa lo que se escribe con el teclado
    BasicTextField(
        value = text,
        onValueChange = { text = it },
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .border(1.dp, Color.Gray)
            .padding(8.dp),
        textStyle = TextStyle(color = Color.Black)
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TextFieldWithContentReceiver() {
    var text by remember { mutableStateOf("") }
    var imageBitmaps by remember { mutableStateOf(listOf<ImageBitmap>()) }
    val context = LocalContext.current

    val contentListener = remember {
        ReceiveContentListener { content ->
            if (content.hasMediaType(MediaType.Image)) {
                // Procesamos imágenes reales leyendo el InputStream desde el ContentResolver
                content.consume { item ->
                    item.uri?.let { uri ->
                        context.contentResolver.openInputStream(uri)?.use { inputStream ->
                            val bitmap = BitmapFactory.decodeStream(inputStream)
                            bitmap?.let {
                                imageBitmaps = imageBitmaps + it.asImageBitmap()
                                return@consume true
                            }
                        }
                    }
                    false
                }
            } else {
                // Para otro tipo de contenido, lo devolvemos sin consumir (se procesará como texto en el BasicTextField)
                content.consume { item ->
                    text = item.text.toString()

                    return@consume true
                }
            }
        }
    }

    Column {
        // Muestra las imágenes recibidas (si hay)
        Row(modifier = Modifier.fillMaxWidth()) {
            imageBitmaps.forEach { bitmap ->
                Image(
                    bitmap = bitmap,
                    contentDescription = "Imagen recibida",
                    modifier = Modifier
                        .size(64.dp)
                        .padding(4.dp)
                )
            }
        }
        // Campo de texto que utiliza contentReceiver para interceptar el contenido entrante
        BasicTextField(
            value = text,
            onValueChange = { text = it },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .border(1.dp, Color.Gray)
                .padding(8.dp)
                .contentReceiver(contentListener),
            textStyle = TextStyle(color = Color.Black)
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ImageDropArea() {
    var droppedImage by remember { mutableStateOf<ImageBitmap?>(null) }
    val context = LocalContext.current

    val imageReceiver = remember {
        ReceiveContentListener { content ->
            if (content.hasMediaType(MediaType.Image)) {
                content.consume { item ->
                    item.uri?.let { uri ->
                        context.contentResolver.openInputStream(uri)?.use { inputStream ->
                            val bitmap = BitmapFactory.decodeStream(inputStream)
                            bitmap?.let {
                                droppedImage = it.asImageBitmap()
                                return@consume true
                            }
                        }
                    }
                    false
                }
            } else {
                content
            }
        }
    }

    Box(
        modifier = Modifier
            .size(200.dp)
            .background(Color(0xFFE0E0E0))
            .border(2.dp, Color.DarkGray)
            .contentReceiver(imageReceiver),
        contentAlignment = Alignment.Center
    ) {
        if (droppedImage != null) {
            Image(
                bitmap = droppedImage!!,
                contentDescription = "Imagen arrastrada",
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Text("Arrastra una imagen aquí", color = Color.DarkGray)
        }
    }
}
