package com.anlarsinsoftware.memoriesbook.ui.theme.Model

data class Comments(val comment:String,val date:String,val user:String,val documentId:String,val postId:String,val isLiked:Boolean=false) {
}