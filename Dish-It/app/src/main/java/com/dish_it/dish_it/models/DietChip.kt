package com.dish_it.dish_it.models

/**
 * Which of the 3 chip rows on My Profile a chip belongs to - drives its
 * colour and icon in DietChipAdapter. Kept separate from the label text
 * since the label is free-form (the person can add their own).
 */
enum class ChipStyle { PREFERENCE, AVOID, CUISINE }

data class DietChip(
    val label: String,
    val style: ChipStyle
)
