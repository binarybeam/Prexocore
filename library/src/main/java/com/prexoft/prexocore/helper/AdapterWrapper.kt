package com.prexoft.prexocore.helper

import androidx.recyclerview.widget.RecyclerView

class AdapterWrapper<T>(
    val adapter: RecyclerView.Adapter<*>,
    val updateItems: (List<T>) -> Unit
)