package com.anlarsinsoftware.memoriesbook.ui.theme.Model

sealed interface ChatItem

data class MessageListItem(val message: Message) : ChatItem

data class DateSeparator(val date: String) : ChatItem