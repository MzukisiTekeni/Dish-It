# Fix Unresolved Reference 'item_diet_chip_add'

The project fails to build because several resources mentioned in `DietChipAdapter.kt` are missing. Specifically, the layout for the "+ Add" chip, its background drawable, and the icons for different chip styles are not present in the project.

## User Review Required

> [!NOTE]
> I will be creating placeholder vector icons for `ic_check`, `ic_block`, and `ic_globe` to resolve the build errors. You may want to replace these with your actual design assets later.

## Proposed Changes

### Resources

I will create the missing layout and drawable resources required by `DietChipAdapter`.

#### [NEW] [bg_pill_dashed.xml](file:///C:/Users/Mzukisi/Documents/GitHub/Dish-It/Dish-It/app/src/main/res/drawable/bg_pill_dashed.xml)
A dashed border drawable for the "Add" chip.

#### [NEW] [item_diet_chip_add.xml](file:///C:/Users/Mzukisi/Documents/GitHub/Dish-It/Dish-It/app/src/main/res/layout/item_diet_chip_add.xml)
The layout for the "+ Add" chip used in the `DietChipAdapter`.

#### [NEW] [ic_check.xml](file:///C:/Users/Mzukisi/Documents/GitHub/Dish-It/Dish-It/app/src/main/res/drawable/ic_check.xml)
A placeholder checkmark icon.

#### [NEW] [ic_block.xml](file:///C:/Users/Mzukisi/Documents/GitHub/Dish-It/Dish-It/app/src/main/res/drawable/ic_block.xml)
A placeholder block icon.

#### [NEW] [ic_globe.xml](file:///C:/Users/Mzukisi/Documents/GitHub/Dish-It/Dish-It/app/src/main/res/drawable/ic_globe.xml)
A placeholder globe icon.

## Verification Plan

### Automated Tests
- Run `./gradlew :app:compileDebugKotlin` to ensure the project builds without unresolved reference errors.

### Manual Verification
- None required as this is a build fix, but the new items should be visible in the UI if deployed.
