package com.example.macrotracker

import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.TextWatcher
import android.text.style.ForegroundColorSpan
import android.transition.AutoTransition
import android.transition.TransitionManager
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.ScrollView
import android.widget.TextView
import androidx.core.content.FileProvider
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

data class FoodEntry(
    val name: String,
    val calories: Double,
    val protein: Double,
    val carbs: Double,
    val fat: Double,
    val grams: Int = 100,
    val loggedAt: String = "",
    val meal: String = "",
    val baseCalories: Double = calories,
    val baseProtein: Double = protein,
    val baseCarbs: Double = carbs,
    val baseFat: Double = fat,
    val portionGrams: Int? = null
) {
    fun toJson(): JSONObject = JSONObject()
        .put("name", name)
        .put("calories", calories)
        .put("protein", protein)
        .put("carbs", carbs)
        .put("fat", fat)
        .put("grams", grams)
        .put("loggedAt", loggedAt)
        .put("meal", meal)
        .put("baseCalories", baseCalories)
        .put("baseProtein", baseProtein)
        .put("baseCarbs", baseCarbs)
        .put("baseFat", baseFat)
        .put("portionGrams", portionGrams ?: -1)

    fun scaledForGrams(
        servingGrams: Int,
        time: String = "",
        mealCategory: String = meal
    ): FoodEntry {
        val factor = servingGrams / 100.0
        return FoodEntry(
            name = name,
            calories = baseCalories * factor,
            protein = baseProtein * factor,
            carbs = baseCarbs * factor,
            fat = baseFat * factor,
            grams = servingGrams,
            loggedAt = time,
            meal = mealCategory,
            baseCalories = baseCalories,
            baseProtein = baseProtein,
            baseCarbs = baseCarbs,
            baseFat = baseFat,
            portionGrams = portionGrams
        )
    }

    companion object {
        fun fromJson(item: JSONObject): FoodEntry = FoodEntry(
            name = item.optString("name", "Alimento"),
            calories = item.optDouble("calories", 0.0),
            protein = item.optDouble("protein", 0.0),
            carbs = item.optDouble("carbs", 0.0),
            fat = item.optDouble("fat", 0.0),
            grams = item.optInt("grams", 100),
            loggedAt = item.optString("loggedAt", "12:00"),
            meal = item.optString("meal", ""),
            baseCalories = item.optDouble("baseCalories", item.optDouble("calories", 0.0)),
            baseProtein = item.optDouble("baseProtein", item.optDouble("protein", 0.0)),
            baseCarbs = item.optDouble("baseCarbs", item.optDouble("carbs", 0.0)),
            baseFat = item.optDouble("baseFat", item.optDouble("fat", 0.0)),
            portionGrams = item.optInt("portionGrams", -1).let { if (it == -1) null else it }
        )
    }
}

data class Goals(
    val calories: Int = 2200,
    val protein: Int = 160,
    val carbs: Int = 240,
    val fat: Int = 70,
    val weightKg: Float = 75f,
    val neckCm: Float = 0f,
    val shouldersCm: Float = 0f,
    val torsoCm: Float = 0f,
    val chestCm: Float = 98f,
    val waistCm: Float = 82f,
    val hipsCm: Float = 96f,
    val rightBicepsCm: Float = 0f,
    val leftBicepsCm: Float = 0f,
    val rightForearmCm: Float = 0f,
    val leftForearmCm: Float = 0f,
    val rightWristCm: Float = 0f,
    val leftWristCm: Float = 0f,
    val rightThighCm: Float = 0f,
    val leftThighCm: Float = 0f,
    val rightCalfCm: Float = 0f,
    val leftCalfCm: Float = 0f,
    val rightAnkleCm: Float = 0f,
    val leftAnkleCm: Float = 0f
)

data class BodyRecord(
    val date: String,
    val weightKg: Float,
    val neckCm: Float,
    val shouldersCm: Float,
    val torsoCm: Float,
    val chestCm: Float,
    val waistCm: Float,
    val hipsCm: Float,
    val rightBicepsCm: Float,
    val leftBicepsCm: Float,
    val rightForearmCm: Float,
    val leftForearmCm: Float,
    val rightWristCm: Float,
    val leftWristCm: Float,
    val rightThighCm: Float,
    val leftThighCm: Float,
    val rightCalfCm: Float,
    val leftCalfCm: Float,
    val rightAnkleCm: Float,
    val leftAnkleCm: Float
) {
    fun toJson(): JSONObject = JSONObject()
        .put("date", date)
        .put("weightKg", weightKg.toDouble())
        .put("neckCm", neckCm.toDouble())
        .put("shouldersCm", shouldersCm.toDouble())
        .put("torsoCm", torsoCm.toDouble())
        .put("chestCm", chestCm.toDouble())
        .put("waistCm", waistCm.toDouble())
        .put("hipsCm", hipsCm.toDouble())
        .put("rightBicepsCm", rightBicepsCm.toDouble())
        .put("leftBicepsCm", leftBicepsCm.toDouble())
        .put("rightForearmCm", rightForearmCm.toDouble())
        .put("leftForearmCm", leftForearmCm.toDouble())
        .put("rightWristCm", rightWristCm.toDouble())
        .put("leftWristCm", leftWristCm.toDouble())
        .put("rightThighCm", rightThighCm.toDouble())
        .put("leftThighCm", leftThighCm.toDouble())
        .put("rightCalfCm", rightCalfCm.toDouble())
        .put("leftCalfCm", leftCalfCm.toDouble())
        .put("rightAnkleCm", rightAnkleCm.toDouble())
        .put("leftAnkleCm", leftAnkleCm.toDouble())

    companion object {
        fun fromJson(item: JSONObject): BodyRecord = BodyRecord(
            date = item.optString("date", LocalDate.now().toString()),
            weightKg = item.optDouble("weightKg", 0.0).toFloat(),
            neckCm = item.optDouble("neckCm", 0.0).toFloat(),
            shouldersCm = item.optDouble("shouldersCm", 0.0).toFloat(),
            torsoCm = item.optDouble("torsoCm", 0.0).toFloat(),
            chestCm = item.optDouble("chestCm", 0.0).toFloat(),
            waistCm = item.optDouble("waistCm", 0.0).toFloat(),
            hipsCm = item.optDouble("hipsCm", 0.0).toFloat(),
            rightBicepsCm = item.optDouble("rightBicepsCm", 0.0).toFloat(),
            leftBicepsCm = item.optDouble("leftBicepsCm", 0.0).toFloat(),
            rightForearmCm = item.optDouble("rightForearmCm", 0.0).toFloat(),
            leftForearmCm = item.optDouble("leftForearmCm", 0.0).toFloat(),
            rightWristCm = item.optDouble("rightWristCm", 0.0).toFloat(),
            leftWristCm = item.optDouble("leftWristCm", 0.0).toFloat(),
            rightThighCm = item.optDouble("rightThighCm", 0.0).toFloat(),
            leftThighCm = item.optDouble("leftThighCm", 0.0).toFloat(),
            rightCalfCm = item.optDouble("rightCalfCm", 0.0).toFloat(),
            leftCalfCm = item.optDouble("leftCalfCm", 0.0).toFloat(),
            rightAnkleCm = item.optDouble("rightAnkleCm", 0.0).toFloat(),
            leftAnkleCm = item.optDouble("leftAnkleCm", 0.0).toFloat()
        )
    }
}

data class Dish(
    val name: String,
    val ingredients: List<FoodEntry>
) {
    fun toJson(): JSONObject {
        val array = JSONArray()
        ingredients.forEach { array.put(it.toJson()) }
        return JSONObject().put("name", name).put("ingredients", array)
    }

    fun toFoodEntry(): FoodEntry {
        val totalKcal = ingredients.sumOf { it.calories }
        val totalProtein = ingredients.sumOf { it.protein }
        val totalCarbs = ingredients.sumOf { it.carbs }
        val totalFat = ingredients.sumOf { it.fat }
        val totalGrams = ingredients.sumOf { it.grams }.coerceAtLeast(1)

        val factor = 100.0 / totalGrams

        return FoodEntry(
            name = name,
            calories = totalKcal,
            protein = totalProtein,
            carbs = totalCarbs,
            fat = totalFat,
            grams = totalGrams,
            baseCalories = totalKcal * factor,
            baseProtein = totalProtein * factor,
            baseCarbs = totalCarbs * factor,
            baseFat = totalFat * factor,
            portionGrams = totalGrams
        )
    }

    companion object {
        fun fromJson(json: JSONObject): Dish {
            val name = json.optString("name", "Piatto")
            val array = json.optJSONArray("ingredients") ?: JSONArray()
            val ingredients = (0 until array.length()).map { FoodEntry.fromJson(array.getJSONObject(it)) }
            return Dish(name, ingredients)
        }
    }
}

enum class Screen {
    Today,
    Foods,
    Progress,
    Settings
}

data class AppPalette(
    val name: String,
    val background: String,
    val card: String,
    val primary: String,
    val secondary: String,
    val text: String,
    val muted: String,
    val accentText: String
)

const val PROTEIN_COLOR = 0xFFE53935.toInt()
const val FAT_COLOR = 0xFFFBC02D.toInt()
const val CARBS_COLOR = 0xFF1E88E5.toInt()

class MainActivity : Activity() {
    private val prefs by lazy { getSharedPreferences("macro_tracker", Context.MODE_PRIVATE) }
    private val entries = mutableListOf<FoodEntry>()
    private val foodLibrary = mutableListOf<FoodEntry>()
    private val dishLibrary = mutableListOf<Dish>()
    private val bodyHistory = mutableListOf<BodyRecord>()
    private val entryDates = linkedSetOf<String>()
    private val dayFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

    private var goals = Goals()
    private var currentDate: LocalDate = LocalDate.now()
    private var screen = Screen.Today
    private var foodsTabMode = 0 // 0: Foods, 1: Dishes
    private var showRemainingMacros = true
    private var useEnglish = false
    private var paletteIndex = 0
    private var librarySearchQuery = ""
    private var isSearching = false
    private var undoAction: (() -> Unit)? = null
    private lateinit var scrollView: ScrollView
    private lateinit var root: LinearLayout
    private lateinit var snackbarContainer: FrameLayout
    private lateinit var macroPanelContent: LinearLayout

    private val palettes = listOf(
        AppPalette("Forest", "#F3F6F1", "#FFFFFF", "#204A3A", "#E1EAE3", "#14211B", "#607069", "#DDEFE6"),
        AppPalette("Midnight", "#101716", "#182220", "#3FA37D", "#263330", "#F2F7F4", "#A7B8B1", "#DDF5EA"),
        AppPalette("Ruby", "#F7F1F2", "#FFFFFF", "#8D2F45", "#F0DDE2", "#241519", "#755D64", "#FFE4EA"),
        AppPalette("Amber", "#121212", "#1E1E1E", "#FF8F00", "#332A20", "#F5F5F5", "#B0B0B0", "#FFF3E0"),
        AppPalette("Ocean", "#F0F4F8", "#FFFFFF", "#1976D2", "#E3F2FD", "#102A43", "#627D98", "#D1E9FF")
    )

    private val palette: AppPalette
        get() = palettes[paletteIndex.coerceIn(0, palettes.lastIndex)]
    private val proteinColor: Int get() = PROTEIN_COLOR
    private val fatColor: Int get() = FAT_COLOR
    private val carbsColor: Int get() = CARBS_COLOR

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableFullScreen()
        initBaseLayout()
        loadData()
        render()
    }

    private fun initBaseLayout() {
        root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(16), dp(16), dp(20))
        }

        scrollView = ScrollView(this).apply {
            addView(root)
            overScrollMode = View.OVER_SCROLL_NEVER
        }

        snackbarContainer = FrameLayout(this).apply {
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT,
                Gravity.BOTTOM
            ).apply { setMargins(dp(16), 0, dp(16), dp(24)) }
        }

        val mainLayout = FrameLayout(this).apply {
            addView(scrollView)
            addView(snackbarContainer)
        }

        setContentView(mainLayout)
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) enableFullScreen()
    }

    private fun render(preserveScroll: Boolean = true) {
        val previousScrollY = if (::scrollView.isInitialized) scrollView.scrollY else 0
        val bgColor = color(palette.background)
        window.decorView.setBackgroundColor(bgColor)
        root.setBackgroundColor(bgColor)
        scrollView.setBackgroundColor(bgColor)

        if (preserveScroll) {
            TransitionManager.beginDelayedTransition(root.parent as android.view.ViewGroup, AutoTransition().apply {
                duration = 150
            })
        }

        root.removeAllViews()
        root.addView(appHeader())
        root.addView(tabRow())

        when (screen) {
            Screen.Today -> renderToday()
            Screen.Foods -> renderFoods()
            Screen.Progress -> renderProgress()
            Screen.Settings -> renderSettings()
        }
        
        if (preserveScroll) {
            scrollView.post { scrollView.scrollTo(0, previousScrollY) }
        } else {
            scrollView.post { scrollView.scrollTo(0, 0) }
        }
    }

    private fun renderToday() {
        root.addView(todayDashboard())
        root.addView(summaryPanel())
        root.addView(macroRows())
        root.addView(sectionTitle(txt("Note della giornata", "Daily notes")))
        root.addView(notesPanel())
        root.addView(sectionTitle(txt("Food log", "Food log")))
        root.addView(foodList())
    }

    private fun notesPanel(): View {
        val note = prefs.getString("notes_${currentDate}", "") ?: ""
        return panel {
            addView(TextView(context).apply {
                text = if (note.isBlank()) txt("Tocca per aggiungere una nota...", "Tap to add a note...") else note
                textSize = 15f
                setTextColor(color(if (note.isBlank()) palette.muted else palette.text))
                setPadding(0, dp(4), 0, dp(4))
            })
            setOnClickListener {
                showInputDialog(txt("Note del giorno", "Daily notes"), listOf(txt("Nota", "Note") to note), onSave = { values ->
                    val newNote = values[0]
                    prefs.edit().putString("notes_${currentDate}", newNote).apply()
                    render()
                })
            }
        }
    }

    private fun renderFoods() {
        root.addView(sectionTitle(txt("Database alimenti e piatti", "Food and dish database")))

        // Tab Toggle
        root.addView(panel {
            addView(LinearLayout(this@MainActivity).apply {
                orientation = LinearLayout.HORIZONTAL
                addView(toggleButton(txt("Alimenti", "Foods"), foodsTabMode == 0) {
                    foodsTabMode = 0
                    render()
                }, weightParams())
                addView(space(8))
                addView(toggleButton(txt("Piatti composti", "Dishes"), foodsTabMode == 1) {
                    foodsTabMode = 1
                    render()
                }, weightParams())
            })
        })

        if (foodsTabMode == 0) {
            renderFoodsTab()
        } else {
            renderDishesTab()
        }
    }

    private fun renderFoodsTab() {
        root.addView(bodyText(txt("Gli alimenti sono salvati con valori riferiti a 100g. Usa lo swipe per eliminare o tieni premuto per modificare.", "Foods are saved per 100g. Swipe to delete or long-press to edit.")))
        
        // Search Bar
        val searchBox = EditText(this).apply {
            hint = txt("Cerca alimento...", "Search food...")
            setHintTextColor(if (palette.name == "Midnight" || palette.name == "Amber") Color.WHITE else Color.GRAY)
            setText(librarySearchQuery)
            setPadding(dp(16), dp(12), dp(16), dp(12))
            background = roundedBackground(palette.card, dp(12))
            setTextColor(color(palette.text))
            setSingleLine(true)
            
            setOnFocusChangeListener { _, hasFocus -> isSearching = hasFocus }

            addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable?) {
                    val query = s?.toString().orEmpty()
                    if (query != librarySearchQuery) {
                        librarySearchQuery = query
                        updateLibraryList(libraryListContainer, librarySearchQuery)
                    }
                }
            })
        }
        root.addView(searchBox)
        
        if (isSearching) {
            searchBox.post {
                searchBox.requestFocus()
                searchBox.setSelection(librarySearchQuery.length)
            }
        }

        root.addView(primaryButton(txt("Nuovo alimento", "New food")) { showFoodDialog(addToDay = false, saveToLibrary = true) })
        
        libraryListContainer = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        root.addView(panel { addView(libraryListContainer) })
        updateLibraryList(libraryListContainer, librarySearchQuery)
    }

    private fun renderDishesTab() {
        root.addView(bodyText(txt("Crea piatti composti da più ingredienti. Usa lo swipe per eliminare.", "Create dishes made of multiple ingredients. Swipe to delete.")))
        root.addView(primaryButton(txt("Nuovo piatto", "New dish")) { showDishDialog() })
        
        val container = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        root.addView(panel { addView(container) })
        
        if (dishLibrary.isEmpty()) {
            container.addView(bodyText(txt("Nessun piatto salvato.", "No dishes saved yet.")))
        } else {
            dishLibrary.sortedBy { it.name.lowercase() }.forEach { dish ->
                container.addView(dishRow(dish))
            }
        }
    }

    private lateinit var libraryListContainer: LinearLayout

    private fun updateLibraryList(container: LinearLayout, query: String) {
        TransitionManager.beginDelayedTransition(container, AutoTransition().apply { duration = 200 })
        container.removeAllViews()
        val filtered = foodLibrary.filter { it.name.startsWith(query, ignoreCase = true) }
        if (filtered.isEmpty()) {
            val msg = if (query.isBlank()) 
                txt("Nessun alimento salvato. Gli alimenti inseriti manualmente vengono salvati qui.", "No saved food yet. Foods entered manually are saved here.")
                else txt("Nessun risultato per la ricerca.", "No results for your search.")
            container.addView(bodyText(msg))
        } else {
            filtered.sortedBy { it.name.lowercase() }.forEach { item ->
                container.addView(libraryRow(item))
            }
        }
    }

    private fun renderProgress() {
        val lastSevenDates = (6 downTo 0).map { LocalDate.now().minusDays(it.toLong()).toString() }
        val kcalValues = lastSevenDates.map { date -> loadEntries(date).sumOf { it.calories }.roundToInt() }
        val recentBody = bodyHistory.sortedBy { it.date }.takeLast(8)
        val weightValues = recentBody.map { it.weightKg.roundToInt() }
        val averageKcal = if (kcalValues.isEmpty()) 0 else kcalValues.average().roundToInt()

        root.addView(sectionTitle(txt("Kcal ultimi 7 giorni", "Last 7 days kcal")))
        root.addView(chart(kcalValues, goals.calories))
        root.addView(panel {
            addView(label(txt("Media settimanale", "Weekly average"), "$averageKcal kcal"))
            lastSevenDates.forEach { date ->
                val total = loadEntries(date).sumOf { it.calories }.roundToInt()
                addView(label(formatDate(date), "$total kcal"))
            }
        })

        root.addView(sectionTitle(txt("Peso", "Weight")))
        root.addView(lineChart(weightValues))
        root.addView(panel {
            if (bodyHistory.isEmpty()) {
                addView(bodyText(txt("Ancora nessun peso salvato.", "No weight saved yet.")))
            } else {
                bodyHistory.sortedByDescending { it.date }.take(10).forEach {
                    addView(label(formatDate(it.date), "${it.weightKg.format()} kg"))
                }
            }
        })
    }

    private fun renderSettings() {
        root.addView(sectionTitle(txt("App", "App")))
        root.addView(appSettingsPanel())
        root.addView(sectionTitle(txt("Calorie e macro", "Calories and macros")))
        root.addView(goalPanel())
        root.addView(primaryButton(txt("Modifica calorie e macro", "Edit calories and macros")) { showMacroGoalsDialog() })
        root.addView(sectionTitle(txt("Peso e misure corpo", "Weight and body measurements")))
        root.addView(bodyPanel())
        root.addView(primaryButton(txt("Registra peso e misure", "Log weight and measurements")) { showBodyDialog() })
        root.addView(sectionTitle(txt("Archivio", "Archive")))
        root.addView(panel {
            addView(bodyText("${txt("Giorni salvati", "Saved days")}: ${entryDates.size}"))
            addView(secondaryButton(txt("Vai a oggi", "Go to today")) {
                currentDate = LocalDate.now()
                entries.replaceWith(loadEntries(currentDate.toString()))
                screen = Screen.Today
                render(preserveScroll = false)
            })
            addView(space(1, 8))
            addView(primaryButton(txt("Esporta in CSV", "Export to CSV")) {
                exportToCSV()
            })
        })
    }

    private fun appSettingsPanel(): View {
        return panel {
            addView(settingRow(txt("Lingua", "Language"), if (useEnglish) "English" else "Italiano"))
            addView(LinearLayout(context).apply {
                orientation = LinearLayout.HORIZONTAL
                addView(secondaryButton(txt("Cambia lingua", "Switch language")) {
                    useEnglish = !useEnglish
                    saveAppSettings()
                    render()
                }, weightParams())
            })
            addView(settingRow(txt("Palette colori", "Color palette"), palette.name))
            
            val chunkedPalettes = palettes.withIndex().chunked(3)
            chunkedPalettes.forEachIndexed { rowIndex, chunk ->
                addView(LinearLayout(context).apply {
                    orientation = LinearLayout.HORIZONTAL
                    chunk.forEachIndexed { colIndex, (index, item) ->
                        addView(toggleButton(item.name, index == paletteIndex) {
                            paletteIndex = index
                            saveAppSettings()
                            render()
                        }, weightParams())
                        if (colIndex != chunk.lastIndex) addView(space(8))
                    }
                })
                if (rowIndex != chunkedPalettes.lastIndex) addView(space(1, 8))
            }
        }
    }

    private fun tabRow(): View {
        val labels = listOf(
            Screen.Today to txt("Oggi", "Today"),
            Screen.Foods to txt("Alimenti", "Foods"),
            Screen.Progress to txt("Progressi", "Progress"),
            Screen.Settings to txt("Impost.", "Settings")
        )
        return LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(0, dp(10), 0, dp(8))
            labels.forEach { (target, label) ->
                addView(
                    button(
                        label,
                        if (screen == target) palette.primary else palette.secondary,
                        if (screen == target) Color.WHITE else color(palette.text)
                    ) {
                        screen = target
                        render(preserveScroll = false)
                    },
                    weightParams()
                )
                if (target != labels.last().first) addView(space(6))
            }
        }
    }

    private fun summaryPanel(): View {
        val consumed = totals()
        val remaining = goals.calories - consumed.calories
        return accentPanel {
            addView(TextView(context).apply {
                text = txt("Kcal assunte", "Calories eaten")
                textSize = 14f
                setTextColor(color(palette.accentText))
            })
            addView(TextView(context).apply {
                text = String.format("%.0f", consumed.calories)
                textSize = 42f
                setTextColor(Color.WHITE)
                setTypeface(typeface, android.graphics.Typeface.BOLD)
            })
            addView(TextView(context).apply {
                text = "${txt("su", "of")} ${goals.calories} kcal"
                textSize = 16f
                setTextColor(color(palette.accentText))
            })
            addView(progress(consumed.calories.roundToInt(), goals.calories))
            addView(TextView(context).apply {
                text = "${txt("Rimanenti", "Left")}: ${String.format("%.0f", remaining.coerceAtLeast(0.0))} kcal"
                textSize = 16f
                setTextColor(Color.WHITE)
                setTypeface(typeface, android.graphics.Typeface.BOLD)
            })
        }
    }

    private fun todayDashboard(): View {
        return panel {
            addView(TextView(context).apply {
                text = txt("Dashboard", "Dashboard")
                textSize = 13f
                setTextColor(color(palette.muted))
            })
            addView(LinearLayout(context).apply {
                orientation = LinearLayout.HORIZONTAL
                setPadding(0, dp(6), 0, dp(8))
                addView(secondaryButton("<") {
                    goToDate(currentDate.minusDays(1), Screen.Today)
                }, weightParams())
                addView(space(8))
                addView(primaryButton(currentDate.format(dayFormatter)) {
                    showDatePicker()
                }, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 2f))
                addView(space(8))
                addView(secondaryButton(">") {
                    goToDate(currentDate.plusDays(1), Screen.Today)
                }, weightParams())
            })
            addView(secondaryButton(txt("Copia da ieri", "Copy from yesterday")) {
                copyFromYesterday()
            }.apply {
                val params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(40))
                params.setMargins(0, dp(4), 0, 0)
                layoutParams = params
            })
        }
    }

    private fun macroRows(): View {
        return panel {
            macroPanelContent = this
            refreshMacroPanel()
        }
    }

    private fun refreshMacroPanel() {
        if (!::macroPanelContent.isInitialized) return
        val consumed = totals()
        macroPanelContent.removeAllViews()
        
        // Header with Toggles
        macroPanelContent.addView(LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(0, 0, 0, dp(12))
            addView(TextView(context).apply {
                text = txt("Macro", "Macros")
                textSize = 19f
                setTextColor(color(palette.text))
                setTypeface(typeface, android.graphics.Typeface.BOLD)
            }, weightParams())
            addView(toggleButton(txt("Assunte", "Eaten"), !showRemainingMacros) {
                showRemainingMacros = false
                refreshMacroPanel()
            })
            addView(space(6))
            addView(toggleButton(txt("Mancanti", "Left"), showRemainingMacros) {
                showRemainingMacros = true
                refreshMacroPanel()
            })
        })

        // Content: Pie Chart + Macro Rows
        macroPanelContent.addView(LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            
            // Pie Chart
            val pieSize = dp(100)
            addView(PieChartView(context).apply {
                this.protein = consumed.protein
                this.carbs = consumed.carbs
                this.fat = consumed.fat
                layoutParams = LinearLayout.LayoutParams(pieSize, pieSize).apply {
                    setMargins(0, 0, dp(20), 0)
                }
            })

            // Macro Rows
            addView(LinearLayout(context).apply {
                orientation = LinearLayout.VERTICAL
                addView(macroRow(txt("Proteine", "Protein"), consumed.protein, goals.protein, "g", proteinColor))
                addView(macroRow(txt("Carboidrati", "Carbs"), consumed.carbs, goals.carbs, "g", carbsColor))
                addView(macroRow(txt("Grassi", "Fat"), consumed.fat, goals.fat, "g", fatColor))
            }, weightParams())
        })
    }

    private fun foodList(): View {
        return panel {
            val mealOrder = listOf(
                txt("Colazione", "Breakfast"),
                txt("Pranzo", "Lunch"),
                txt("Cena", "Dinner"),
                txt("Spuntini", "Snacks")
            )

            mealOrder.forEach { mealName ->
                val items = entries.withIndex()
                    .filter { it.value.meal == mealName }
                
                val mealKcal = items.sumOf { it.value.calories }
                val mealProtein = items.sumOf { it.value.protein }
                val mealCarbs = items.sumOf { it.value.carbs }
                val mealFat = items.sumOf { it.value.fat }
                
                // Meal Header
                addView(LinearLayout(context).apply {
                    orientation = LinearLayout.HORIZONTAL
                    gravity = Gravity.CENTER_VERTICAL
                    setPadding(0, dp(12), 0, dp(4))
                    
                    addView(TextView(context).apply {
                        text = mealName
                        textSize = 14f
                        setTextColor(palette.primary.let { color(it) })
                        setTypeface(typeface, android.graphics.Typeface.BOLD)
                    }, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
                    
                    addView(TextView(context).apply {
                        textSize = 13f
                        setTextColor(color(palette.muted))
                        setPadding(0, 0, dp(12), 0)
                        text = SpannableStringBuilder().apply {
                            append("${mealKcal.roundToInt()} kcal | ")
                            appendMacroToken("P ${mealProtein.roundToInt()}g", proteinColor)
                            append(" ")
                            appendMacroToken("C ${mealCarbs.roundToInt()}g", carbsColor)
                            append(" ")
                            appendMacroToken("G ${mealFat.roundToInt()}g", fatColor)
                        }
                    })
                    
                    addView(TextView(context).apply {
                        text = " + "
                        textSize = 20f
                        setTextColor(palette.primary.let { color(it) })
                        setTypeface(typeface, android.graphics.Typeface.BOLD)
                        setPadding(dp(8), dp(4), dp(8), dp(4))
                        background = roundedBackground(palette.secondary, dp(8))
                        setOnClickListener { showAddOptionsForMeal(mealName) }
                    })
                })
                
                if (items.isEmpty()) {
                    addView(bodyText(txt("Nessun alimento", "No items")))
                } else {
                    items.forEach { item ->
                        addView(foodRow(item.value, item.index))
                    }
                }
                
                if (mealName != mealOrder.last()) {
                    addView(View(context).apply {
                        layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 1)
                        setBackgroundColor(color(palette.secondary))
                        alpha = 0.5f
                    })
                }
            }
        }
    }

    private fun showAddOptionsForMeal(mealName: String) {
        val options = arrayOf(txt("+ Alimento", "+ Food"), txt("Database", "Database"), txt("Piatti", "Dishes"))
        AlertDialog.Builder(this)
            .setTitle(mealName)
            .setItems(options) { _, which ->
                when (which) {
                    0 -> showFoodDialog(addToDay = true, saveToLibrary = false, targetMeal = mealName)
                    1 -> showLibraryPicker(targetMeal = mealName)
                    2 -> showDishPicker(targetMeal = mealName)
                }
            }
            .show()
    }

    private fun getMealNameFallback(time: String): String {
        val hour = time.split(":")[0].toIntOrNull() ?: 0
        return when (hour) {
            in 5..10 -> txt("Colazione", "Breakfast")
            in 11..15 -> txt("Pranzo", "Lunch")
            in 19..23 -> txt("Cena", "Dinner")
            else -> txt("Spuntini", "Snacks")
        }
    }

    private fun bodyPanel(): View {
        return panel {
            addView(settingRow(txt("Peso attuale", "Current weight"), "${goals.weightKg.format()} kg"))
            addView(settingRow(txt("Collo", "Neck"), "${goals.neckCm.format()} cm"))
            addView(settingRow(txt("Spalle", "Shoulders"), "${goals.shouldersCm.format()} cm"))
            addView(settingRow(txt("Torace", "Torso"), "${goals.torsoCm.format()} cm"))
            addView(settingRow(txt("Petto", "Chest"), "${goals.chestCm.format()} cm"))
            addView(settingRow(txt("Circonferenza vita", "Waist"), "${goals.waistCm.format()} cm"))
            addView(settingRow(txt("Circonferenza fianchi", "Hips"), "${goals.hipsCm.format()} cm"))
            addView(settingRow(txt("Bicipite destro", "Right biceps"), "${goals.rightBicepsCm.format()} cm"))
            addView(settingRow(txt("Bicipite sinistro", "Left biceps"), "${goals.leftBicepsCm.format()} cm"))
            addView(settingRow(txt("Avambraccio destro", "Right forearm"), "${goals.rightForearmCm.format()} cm"))
            addView(settingRow(txt("Avambraccio sinistro", "Left forearm"), "${goals.leftForearmCm.format()} cm"))
            addView(settingRow(txt("Polso destro", "Right wrist"), "${goals.rightWristCm.format()} cm"))
            addView(settingRow(txt("Polso sinistro", "Left wrist"), "${goals.leftWristCm.format()} cm"))
            addView(settingRow(txt("Coscia destra", "Right thigh"), "${goals.rightThighCm.format()} cm"))
            addView(settingRow(txt("Coscia sinistra", "Left thigh"), "${goals.leftThighCm.format()} cm"))
            addView(settingRow(txt("Polpaccio destro", "Right calf"), "${goals.rightCalfCm.format()} cm"))
            addView(settingRow(txt("Polpaccio sinistro", "Left calf"), "${goals.leftCalfCm.format()} cm"))
            addView(settingRow(txt("Caviglia destra", "Right ankle"), "${goals.rightAnkleCm.format()} cm"))
            addView(settingRow(txt("Caviglia sinistra", "Left ankle"), "${goals.leftAnkleCm.format()} cm"))
        }
    }

    private fun goalPanel(): View {
        return panel {
            addView(settingRow(txt("Obiettivo calorie giornaliere", "Daily calorie goal"), "${goals.calories} kcal"))
            addView(macroSettingRow(txt("Obiettivo proteine", "Protein goal"), "${goals.protein} g", proteinColor))
            addView(macroSettingRow(txt("Obiettivo carboidrati", "Carbs goal"), "${goals.carbs} g", carbsColor))
            addView(macroSettingRow(txt("Obiettivo grassi", "Fat goal"), "${goals.fat} g", fatColor))
        }
    }

    private fun foodRow(entry: FoodEntry, index: Int): View {
        var startX = 0f
        var startY = 0f
        var isSwiping = false
        val touchSlop = ViewConfiguration.get(this).scaledTouchSlop

        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(0, dp(10), 0, dp(10))
            addView(rowTitle(entry.name))
            addView(macroSummaryText("${entry.grams}g | ${String.format("%.1f", entry.calories)} kcal", entry.protein, entry.carbs, entry.fat))
            
            setOnLongClickListener {
                showEditLoggedFoodDialog(index)
                true
            }

            setOnTouchListener { view, event ->
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        startX = event.rawX
                        startY = event.rawY
                        isSwiping = false
                        false
                    }
                    MotionEvent.ACTION_MOVE -> {
                        val dx = event.rawX - startX
                        val dy = event.rawY - startY
                        if (!isSwiping && abs(dx) > touchSlop && abs(dx) > abs(dy)) {
                            isSwiping = true
                            parent.requestDisallowInterceptTouchEvent(true)
                            
                            // Cancel long click by sending ACTION_CANCEL to the view
                            val cancelEvent = MotionEvent.obtain(event)
                            cancelEvent.action = MotionEvent.ACTION_CANCEL
                            view.onTouchEvent(cancelEvent)
                            cancelEvent.recycle()
                        }
                        
                        if (isSwiping) {
                            view.translationX = dx
                            true
                        } else {
                            false
                        }
                    }
                    MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                        if (isSwiping) {
                            val dx = event.rawX - startX
                            if (abs(dx) > dp(96)) {
                                deleteLoggedFood(index)
                            } else {
                                view.animate()
                                    .translationX(0f)
                                    .setDuration(200)
                                    .start()
                            }
                            isSwiping = false
                            true
                        } else {
                            if (event.action == MotionEvent.ACTION_UP) {
                                view.performClick()
                            }
                            false
                        }
                    }
                    else -> false
                }
            }
        }
    }

    private fun libraryRow(entry: FoodEntry): View {
        var startX = 0f
        var startY = 0f
        var isSwiping = false
        val touchSlop = ViewConfiguration.get(this).scaledTouchSlop

        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(0, dp(10), 0, dp(10))
            addView(rowTitle(entry.name))
            
            val isPortion = entry.portionGrams != null
            val labelPrefix = if (isPortion) "${txt("Per porzione", "Per portion")} (${entry.portionGrams}g)" else "${txt("Per", "Per")} 100g"
            val factor = if (isPortion) (entry.portionGrams!! / 100.0) else 1.0
            
            addView(macroSummaryText("$labelPrefix: ${String.format("%.1f", entry.baseCalories * factor)} kcal", 
                entry.baseProtein * factor, entry.baseCarbs * factor, entry.baseFat * factor))
            
            setOnLongClickListener {
                showFoodDialog(addToDay = false, saveToLibrary = true, existingEntry = entry)
                true
            }

            setOnTouchListener { view, event ->
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        startX = event.rawX
                        startY = event.rawY
                        isSwiping = false
                        false
                    }
                    MotionEvent.ACTION_MOVE -> {
                        val dx = event.rawX - startX
                        val dy = event.rawY - startY
                        if (!isSwiping && abs(dx) > touchSlop && abs(dx) > abs(dy)) {
                            isSwiping = true
                            parent.requestDisallowInterceptTouchEvent(true)
                            
                            val cancelEvent = MotionEvent.obtain(event)
                            cancelEvent.action = MotionEvent.ACTION_CANCEL
                            view.onTouchEvent(cancelEvent)
                            cancelEvent.recycle()
                        }
                        
                        if (isSwiping) {
                            view.translationX = dx
                            true
                        } else {
                            false
                        }
                    }
                    MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                        if (isSwiping) {
                            val dx = event.rawX - startX
                            if (abs(dx) > dp(96)) {
                                val item = entry
                                foodLibrary.remove(entry)
                                saveFoodLibrary()
                                
                                // Defer render to ensure touch event completion
                                view.post { 
                                    render() 
                                    showUndoSnackbar(txt("Alimento rimosso dal database", "Food removed from database")) {
                                        foodLibrary.add(item)
                                        saveFoodLibrary()
                                        render()
                                    }
                                }
                            } else {
                                view.animate()
                                    .translationX(0f)
                                    .setDuration(200)
                                    .start()
                            }
                            isSwiping = false
                            true
                        } else {
                            false
                        }
                    }
                    else -> false
                }
            }
        }
    }

    private fun macroRow(name: String, current: Double, target: Int, unit: String, macroColor: Int): View {
        val remaining = (target - current).coerceAtLeast(0.0)
        val shown = if (showRemainingMacros) remaining else current
        val caption = if (showRemainingMacros) {
            "${txt("assunte", "eaten")}: ${String.format("%.1f", current)} / $target $unit"
        } else {
            "${txt("mancanti", "left")}: ${String.format("%.1f", remaining)} $unit"
        }
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(0, dp(6), 0, dp(10))
            addView(LinearLayout(context).apply {
                orientation = LinearLayout.HORIZONTAL
                addView(TextView(context).apply {
                    text = name
                    textSize = 15f
                    setTextColor(macroColor)
                    setTypeface(typeface, android.graphics.Typeface.BOLD)
                }, weightParams())
                addView(TextView(context).apply {
                    text = "${String.format("%.1f", shown)} $unit"
                    textSize = 15f
                    gravity = Gravity.END
                    setTextColor(macroColor)
                    setTypeface(typeface, android.graphics.Typeface.BOLD)
                }, weightParams())
            })
            addView(progress(shown.roundToInt(), target))
            addView(bodyText(caption))
        }
    }

    private fun showFoodDialog(addToDay: Boolean, saveToLibrary: Boolean, existingEntry: FoodEntry? = null, targetMeal: String = "") {
        var isPortionBase = existingEntry?.portionGrams != null
        val suffix = if (isPortionBase) txt(" per porzione", " per portion") else txt(" per 100g", " per 100g")
        val refWeightLabel = if (isPortionBase) txt("Peso porzione (g)", "Portion weight (g)") else txt("Peso riferimento (g)", "Ref. weight (g)")
        
        val refWeightValue = existingEntry?.portionGrams ?: 100
        val factor = if (isPortionBase) (refWeightValue / 100.0) else 1.0

        val fields = mutableListOf(
            txt("Nome alimento", "Food name") to (existingEntry?.name ?: ""),
            refWeightLabel to refWeightValue.toString(),
            (txt("Kcal", "Kcal") + suffix) to (existingEntry?.let { String.format("%.1f", it.baseCalories * factor) } ?: ""),
            (txt("Proteine", "Protein") + suffix) to (existingEntry?.let { String.format("%.1f", it.baseProtein * factor) } ?: ""),
            (txt("Carboidrati", "Carbs") + suffix) to (existingEntry?.let { String.format("%.1f", it.baseCarbs * factor) } ?: ""),
            (txt("Grassi", "Fat") + suffix) to (existingEntry?.let { String.format("%.1f", it.baseFat * factor) } ?: "")
        )
        if (addToDay) {
            fields.add(txt("Grammi consumati", "Grams eaten") to "100")
        }

        showInputDialog(
            if (existingEntry == null) txt("Aggiungi alimento", "Add food") 
            else txt("Modifica alimento", "Edit food"), 
            fields,
            customHeader = { container ->
                val radioGroup = RadioGroup(this).apply {
                    orientation = RadioGroup.HORIZONTAL
                    setPadding(0, 0, 0, dp(16))
                    
                    val rb100g = RadioButton(this@MainActivity).apply {
                        text = "100g"
                        setTextColor(color(palette.text))
                        buttonTintList = ColorStateList.valueOf(color(palette.primary))
                        id = View.generateViewId()
                        isChecked = !isPortionBase
                    }
                    val rbPortion = RadioButton(this@MainActivity).apply {
                        text = txt("Porzione", "Portion")
                        setTextColor(color(palette.text))
                        buttonTintList = ColorStateList.valueOf(color(palette.primary))
                        id = View.generateViewId()
                        isChecked = isPortionBase
                    }
                    
                    addView(rb100g)
                    addView(rbPortion)
                    
                    setOnCheckedChangeListener { _, checkedId ->
                        isPortionBase = checkedId == rbPortion.id
                        val newSuffix = if (isPortionBase) txt(" per porzione", " per portion") else txt(" per 100g", " per 100g")
                        val newRefLabel = if (isPortionBase) txt("Peso porzione (g)", "Portion weight (g)") else txt("Peso riferimento (g)", "Ref. weight (g)")
                        
                        try {
                            val scrollView = container.getChildAt(2) as? ScrollView
                            val fieldsLayout = scrollView?.getChildAt(0) as? LinearLayout
                            if (fieldsLayout != null) {
                                // Update label and hint for Ref Weight
                                (fieldsLayout.getChildAt(3) as? TextView)?.text = newRefLabel
                                (fieldsLayout.getChildAt(4) as? EditText)?.hint = newRefLabel
                                
                                // Update label and hint for Kcal
                                val kcalLabel = txt("Kcal", "Kcal") + newSuffix
                                (fieldsLayout.getChildAt(6) as? TextView)?.text = kcalLabel
                                (fieldsLayout.getChildAt(7) as? EditText)?.hint = kcalLabel
                                
                                // Update label and hint for Protein
                                val protLabel = txt("Proteine", "Protein") + newSuffix
                                (fieldsLayout.getChildAt(9) as? TextView)?.text = protLabel
                                (fieldsLayout.getChildAt(10) as? EditText)?.hint = protLabel
                                
                                // Update label and hint for Carbs
                                val carbsLabel = txt("Carboidrati", "Carbs") + newSuffix
                                (fieldsLayout.getChildAt(12) as? TextView)?.text = carbsLabel
                                (fieldsLayout.getChildAt(13) as? EditText)?.hint = carbsLabel
                                
                                // Update label and hint for Fat
                                val fatLabel = txt("Grassi", "Fat") + newSuffix
                                (fieldsLayout.getChildAt(15) as? TextView)?.text = fatLabel
                                (fieldsLayout.getChildAt(16) as? EditText)?.hint = fatLabel
                            }
                        } catch (e: Exception) {}
                    }
                }
                container.addView(radioGroup, 1) // After title
            }
        ) { values ->
            val refWeight = values[1].toDoubleValue(100.0).coerceAtLeast(1.0)
            val factor = 100.0 / refWeight
            
            val baseEntry = FoodEntry(
                name = values[0].ifBlank { txt("Alimento", "Food") },
                calories = values[2].toDoubleValue() * factor,
                protein = values[3].toDoubleValue() * factor,
                carbs = values[4].toDoubleValue() * factor,
                fat = values[5].toDoubleValue() * factor,
                grams = 100,
                loggedAt = "",
                meal = targetMeal,
                portionGrams = if (isPortionBase) refWeight.roundToInt() else null
            )
            if (addToDay) {
                val grams = values.getOrNull(6).orEmpty().toDoubleValue(100.0).roundToInt().coerceAtLeast(1)
                addEntryToDay(baseEntry.scaledForGrams(grams, "", targetMeal))
            }
            if (saveToLibrary) {
                // If editing, remove the old one first (handles name changes)
                existingEntry?.let { old ->
                    foodLibrary.removeAll { it.name.equals(old.name, ignoreCase = true) }
                }
                saveFoodInLibrary(baseEntry)
            }
            render()
        }
    }

    private fun showServingDialog(entry: FoodEntry, targetMeal: String = "", onResult: ((FoodEntry) -> Unit)? = null) {
        var isPortionMode = entry.portionGrams != null
        val fields = if (isPortionMode) {
            listOf(txt("Numero di porzioni", "Number of portions") to "1")
        } else {
            listOf(txt("Grammi da aggiungere", "Grams to add") to "100")
        }

        showInputDialog(
            txt("Quantità", "Quantity"), 
            fields,
            customHeader = { container ->
                if (entry.portionGrams != null) {
                    val radioGroup = RadioGroup(this).apply {
                        orientation = RadioGroup.HORIZONTAL
                        setPadding(0, 0, 0, dp(16))
                        
                        val rbGrams = RadioButton(this@MainActivity).apply {
                            text = txt("Grammi", "Grams")
                            setTextColor(color(palette.text))
                            buttonTintList = ColorStateList.valueOf(color(palette.primary))
                            id = View.generateViewId()
                        }
                        val rbPortion = RadioButton(this@MainActivity).apply {
                            text = txt("Porzioni", "Portions")
                            setTextColor(color(palette.text))
                            buttonTintList = ColorStateList.valueOf(color(palette.primary))
                            id = View.generateViewId()
                            isChecked = true
                        }
                        
                        addView(rbGrams)
                        addView(rbPortion)
                        
                        setOnCheckedChangeListener { _, checkedId ->
                            isPortionMode = checkedId == rbPortion.id
                            try {
                                val scrollView = container.getChildAt(2) as? ScrollView
                                val fieldsLayout = scrollView?.getChildAt(0) as? LinearLayout
                                if (fieldsLayout != null) {
                                    val label = fieldsLayout.getChildAt(0) as? TextView
                                    val input = fieldsLayout.getChildAt(1) as? EditText
                                    if (isPortionMode) {
                                        label?.text = txt("Numero di porzioni", "Number of portions")
                                        input?.setText("1")
                                    } else {
                                        label?.text = txt("Grammi da aggiungere", "Grams to add")
                                        input?.setText("100")
                                    }
                                }
                            } catch (e: Exception) {}
                        }
                    }
                    container.addView(radioGroup, 1)
                }
            },
            onSave = { values ->
                val quantity = values[0].toDoubleValue(1.0)
                val totalGrams = if (isPortionMode && entry.portionGrams != null) {
                    (quantity * entry.portionGrams).roundToInt()
                } else {
                    quantity.roundToInt()
                }
                
                val scaled = entry.scaledForGrams(totalGrams.coerceAtLeast(1), "", targetMeal.ifBlank { entry.meal })
                
                if (onResult != null) {
                    onResult(scaled)
                } else {
                    addEntryToDay(scaled)
                    showUndoSnackbar(txt("Aggiunto: ${entry.name}", "Added: ${entry.name}")) {
                        if (entries.isNotEmpty()) {
                            entries.removeAt(entries.lastIndex)
                            saveEntriesForCurrentDay()
                            render()
                        }
                    }
                    render()
                }
            }
        )
    }

    private fun showEditLoggedFoodDialog(index: Int) {
        val entry = entries.getOrNull(index) ?: return
        var isPortionMode = entry.portionGrams != null
        val fields = if (isPortionMode) {
            val portions = entry.grams.toDouble() / (entry.portionGrams ?: entry.grams)
            listOf(txt("Numero di porzioni", "Number of portions") to String.format("%.2f", portions))
        } else {
            listOf(txt("Grammi", "Grams") to entry.grams.toString())
        }

        showInputDialog(
            txt("Correggi alimento", "Edit food"),
            fields,
            customHeader = { container ->
                if (entry.portionGrams != null) {
                    val radioGroup = RadioGroup(this).apply {
                        orientation = RadioGroup.HORIZONTAL
                        setPadding(0, 0, 0, dp(16))
                        
                        val rbGrams = RadioButton(this@MainActivity).apply {
                            text = txt("Grammi", "Grams")
                            setTextColor(color(palette.text))
                            buttonTintList = ColorStateList.valueOf(color(palette.primary))
                            id = View.generateViewId()
                        }
                        val rbPortion = RadioButton(this@MainActivity).apply {
                            text = txt("Porzioni", "Portions")
                            setTextColor(color(palette.text))
                            buttonTintList = ColorStateList.valueOf(color(palette.primary))
                            id = View.generateViewId()
                            isChecked = true
                        }
                        
                        addView(rbGrams)
                        addView(rbPortion)
                        
                        setOnCheckedChangeListener { _, checkedId ->
                            isPortionMode = checkedId == rbPortion.id
                            try {
                                val scrollView = container.getChildAt(2) as? ScrollView
                                val fieldsLayout = scrollView?.getChildAt(0) as? LinearLayout
                                if (fieldsLayout != null) {
                                    val label = fieldsLayout.getChildAt(0) as? TextView
                                    val input = fieldsLayout.getChildAt(1) as? EditText
                                    if (isPortionMode) {
                                        label?.text = txt("Numero di porzioni", "Number of portions")
                                        val portions = entry.grams.toDouble() / (entry.portionGrams ?: entry.grams)
                                        input?.setText(String.format("%.2f", portions))
                                    } else {
                                        label?.text = txt("Grammi", "Grams")
                                        input?.setText(entry.grams.toString())
                                    }
                                }
                            } catch (e: Exception) {}
                        }
                    }
                    container.addView(radioGroup, 1)
                }
            },
            onSave = { values ->
                val quantity = values[0].toDoubleValue(entry.grams.toDouble())
                val totalGrams = if (isPortionMode && entry.portionGrams != null) {
                    (quantity * entry.portionGrams).roundToInt()
                } else {
                    quantity.roundToInt()
                }
                
                entries[index] = entry.scaledForGrams(totalGrams.coerceAtLeast(1), "", entry.meal)
                saveEntriesForCurrentDay()
                render()
            }
        )
    }

    private fun deleteLoggedFood(index: Int) {
        if (index !in entries.indices) return
        val item = entries[index]
        val idx = index
        entries.removeAt(index)
        saveEntriesForCurrentDay()
        
        window.decorView.post { 
            render() 
            // Show snackbar AFTER render so it doesn't get cleared
            showUndoSnackbar(txt("Alimento eliminato", "Food deleted")) {
                entries.add(idx, item)
                saveEntriesForCurrentDay()
                render()
            }
        }
    }

    private fun showLibraryPicker(targetMeal: String = "", onSelected: ((FoodEntry) -> Unit)? = null) {
        if (foodLibrary.isEmpty()) {
            AlertDialog.Builder(this)
                .setTitle(txt("Database vuoto", "Database empty"))
                .setMessage(txt("Aggiungi prima un alimento manualmente o dalla sezione Alimenti.", "Add a food manually or from the Foods section first."))
                .setPositiveButton("Ok", null)
                .show()
            return
        }

        val dialog = AlertDialog.Builder(this).create()
        var currentQuery = ""
        
        fun createPickerView(): View {
            return LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setBackgroundColor(color(palette.background))
                setPadding(dp(24), dp(24), dp(24), dp(24))
                
                addView(TextView(context).apply {
                    text = txt("Aggiungi dal database", "Add from database")
                    textSize = 22f
                    setTextColor(color(palette.text))
                    setTypeface(typeface, android.graphics.Typeface.BOLD)
                    setPadding(0, 0, 0, dp(16))
                })

                val searchInput = EditText(context).apply {
                    hint = txt("Cerca...", "Search...")
                    setHintTextColor(if (palette.name == "Midnight" || palette.name == "Amber") Color.WHITE else Color.GRAY)
                    setPadding(dp(12), dp(12), dp(12), dp(12))
                    background = roundedBackground(palette.card, dp(12))
                    setTextColor(color(palette.text))
                    setSingleLine(true)
                }
                addView(searchInput)
                addView(space(1, 12))

                val scroll = ScrollView(context).apply {
                    overScrollMode = View.OVER_SCROLL_NEVER
                }
                val listContainer = LinearLayout(context).apply { orientation = LinearLayout.VERTICAL }
                
                fun updateList(query: String) {
                    listContainer.removeAllViews()
                    val filtered = foodLibrary.filter { it.name.startsWith(query, ignoreCase = true) }
                        .sortedBy { it.name.lowercase() }
                    
                    if (filtered.isEmpty()) {
                        listContainer.addView(bodyText(txt("Nessun risultato", "No results")))
                    } else {
                        filtered.forEach { entry ->
                            listContainer.addView(LinearLayout(context).apply {
                                orientation = LinearLayout.VERTICAL
                                setPadding(0, dp(12), 0, dp(12))
                                background = roundedBackground(palette.background, 0)
                                addView(rowTitle(entry.name))
                                
                                val isPortion = entry.portionGrams != null
                                val labelPrefix = if (isPortion) "${txt("Per porzione", "Per portion")} (${entry.portionGrams}g)" else "${txt("Per", "Per")} 100g"
                                val factor = if (isPortion) (entry.portionGrams!!.toDouble() / 100.0) else 1.0
                                
                                addView(macroSummaryText("$labelPrefix: ${String.format("%.1f", entry.baseCalories * factor)} kcal", 
                                    entry.baseProtein * factor, entry.baseCarbs * factor, entry.baseFat * factor))

                                setOnClickListener {
                                    if (onSelected != null) {
                                        onSelected(entry)
                                        dialog.dismiss()
                                    } else {
                                        showServingDialog(entry, targetMeal)
                                        dialog.dismiss()
                                    }
                                }
                            })
                        }
                    }
                }

                searchInput.addTextChangedListener(object : TextWatcher {
                    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                    override fun afterTextChanged(s: Editable?) {
                        currentQuery = s?.toString().orEmpty()
                        updateList(currentQuery)
                    }
                })

                updateList("")
                scroll.addView(listContainer)
                addView(scroll, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f))
                
                addView(secondaryButton(txt("Chiudi", "Close")) { dialog.dismiss() }.apply {
                    val params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(46))
                    params.setMargins(0, dp(16), 0, 0)
                    layoutParams = params
                })
            }
        }

        dialog.setView(createPickerView())
        dialog.show()
        dialog.window?.setBackgroundDrawable(roundedBackground(palette.background, dp(24)))
    }

    private fun showDatePicker() {
        DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                goToDate(LocalDate.of(year, month + 1, dayOfMonth), Screen.Today)
            },
            currentDate.year,
            currentDate.monthValue - 1,
            currentDate.dayOfMonth
        ).show()
    }

    private fun goToDate(date: LocalDate, targetScreen: Screen = screen) {
        saveEntriesForCurrentDay()
        currentDate = date
        entries.replaceWith(loadEntries(currentDate.toString()))
        screen = targetScreen
        render(preserveScroll = false)
    }

    private fun showMacroGoalsDialog() {
        val fields = listOf(
            txt("Kcal giornaliere", "Daily kcal") to goals.calories.toString(),
            txt("Proteine g", "Protein g") to goals.protein.toString(),
            txt("Carboidrati g", "Carbs g") to goals.carbs.toString(),
            txt("Grassi g", "Fat g") to goals.fat.toString()
        )
        showInputDialog(txt("Calorie e macro", "Calories and macros"), fields, onSave = { values ->
            goals = goals.copy(
                calories = values[0].toIntValue(goals.calories),
                protein = values[1].toIntValue(goals.protein),
                carbs = values[2].toIntValue(goals.carbs),
                fat = values[3].toIntValue(goals.fat)
            )
            saveGoals()
            render()
        })
    }

    private fun showBodyDialog() {
        val fields = listOf(
            txt("Peso kg", "Weight kg") to goals.weightKg.format(),
            txt("Collo cm", "Neck cm") to goals.neckCm.format(),
            txt("Spalle cm", "Shoulders cm") to goals.shouldersCm.format(),
            txt("Torace cm", "Torso cm") to goals.torsoCm.format(),
            txt("Petto cm", "Chest cm") to goals.chestCm.format(),
            txt("Vita cm", "Waist cm") to goals.waistCm.format(),
            txt("Fianchi cm", "Hips cm") to goals.hipsCm.format(),
            txt("Bicipite destro cm", "Right biceps cm") to goals.rightBicepsCm.format(),
            txt("Bicipite sinistro cm", "Left biceps cm") to goals.leftBicepsCm.format(),
            txt("Avambraccio destro cm", "Right forearm cm") to goals.rightForearmCm.format(),
            txt("Avambraccio sinistro cm", "Left forearm cm") to goals.leftForearmCm.format(),
            txt("Polso destro cm", "Right wrist cm") to goals.rightWristCm.format(),
            txt("Polso sinistro cm", "Left wrist cm") to goals.leftWristCm.format(),
            txt("Coscia destra cm", "Right thigh cm") to goals.rightThighCm.format(),
            txt("Coscia sinistra cm", "Left thigh cm") to goals.leftThighCm.format(),
            txt("Polpaccio destro cm", "Right calf cm") to goals.rightCalfCm.format(),
            txt("Polpaccio sinistro cm", "Left calf cm") to goals.leftCalfCm.format(),
            txt("Caviglia destra cm", "Right ankle cm") to goals.rightAnkleCm.format(),
            txt("Caviglia sinistra cm", "Left ankle cm") to goals.leftAnkleCm.format()
        )
        showInputDialog(txt("Peso e misure", "Weight and measurements"), fields, onSave = { values ->
            goals = goals.copy(
                weightKg = values[0].toFloatValue(goals.weightKg),
                neckCm = values[1].toFloatValue(goals.neckCm),
                shouldersCm = values[2].toFloatValue(goals.shouldersCm),
                torsoCm = values[3].toFloatValue(goals.torsoCm),
                chestCm = values[4].toFloatValue(goals.chestCm),
                waistCm = values[5].toFloatValue(goals.waistCm),
                hipsCm = values[6].toFloatValue(goals.hipsCm),
                rightBicepsCm = values[7].toFloatValue(goals.rightBicepsCm),
                leftBicepsCm = values[8].toFloatValue(goals.leftBicepsCm),
                rightForearmCm = values[9].toFloatValue(goals.rightForearmCm),
                leftForearmCm = values[10].toFloatValue(goals.leftForearmCm),
                rightWristCm = values[11].toFloatValue(goals.rightWristCm),
                leftWristCm = values[12].toFloatValue(goals.leftWristCm),
                rightThighCm = values[13].toFloatValue(goals.rightThighCm),
                leftThighCm = values[14].toFloatValue(goals.leftThighCm),
                rightCalfCm = values[15].toFloatValue(goals.rightCalfCm),
                leftCalfCm = values[16].toFloatValue(goals.leftCalfCm),
                rightAnkleCm = values[17].toFloatValue(goals.rightAnkleCm),
                leftAnkleCm = values[18].toFloatValue(goals.leftAnkleCm)
            )
            saveGoals()
            saveBodyRecord()
            render()
        })
    }

    private fun showInputDialog(
        title: String,
        fields: List<Pair<String, String>>,
        customHeader: (LinearLayout) -> Unit = {},
        onSave: (List<String>) -> Unit
    ) {
        val dialog = AlertDialog.Builder(this).create()
        val inputs = mutableListOf<EditText>()
        
        val dialogView = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(color(palette.background))
            setPadding(dp(24), dp(24), dp(24), dp(24))
            
            // Header
            addView(TextView(context).apply {
                text = title
                textSize = 22f
                setTextColor(color(palette.text))
                setTypeface(typeface, android.graphics.Typeface.BOLD)
                setPadding(0, 0, 0, dp(16))
            })

            customHeader(this)

            val scrollContainer = ScrollView(context).apply {
                overScrollMode = View.OVER_SCROLL_NEVER
                isVerticalScrollBarEnabled = false
            }
            
            val fieldsLayout = LinearLayout(context).apply {
                orientation = LinearLayout.VERTICAL
                
                var i = 0
                while (i < fields.size) {
                    val field = fields[i]
                    val labelView = fieldLabel(field.first)
                    macroColorForLabel(field.first)?.let { labelView.setTextColor(it) }
                    addView(labelView)
                    
                    val input = EditText(context).apply {
                        hint = field.first
                        setHintTextColor(if (palette.name == "Midnight" || palette.name == "Amber") Color.WHITE else Color.GRAY)
                        setText(field.second)
                        setSingleLine(field.first != txt("Nota", "Note"))
                        inputType = if (field.first == txt("Nome alimento", "Food name") || field.first == txt("Nota", "Note")) {
                            InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
                        } else {
                            InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
                        }
                        if (field.first == txt("Nota", "Note")) {
                            minLines = 3
                            gravity = Gravity.TOP
                        }
                        setPadding(dp(12), dp(12), dp(12), dp(12))
                        background = roundedBackground(palette.card, dp(12))
                        setTextColor(color(palette.text))
                    }
                    addView(input)
                    addView(space(1, 12))
                    inputs.add(input)
                    i++
                }
            }
            
            scrollContainer.addView(fieldsLayout)
            addView(scrollContainer, LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f
            ))
            
            // Buttons
            addView(LinearLayout(context).apply {
                orientation = LinearLayout.HORIZONTAL
                setPadding(0, dp(20), 0, 0)
                
                addView(secondaryButton(txt("Annulla", "Cancel")) {
                    dialog.dismiss()
                }, weightParams())
                
                addView(space(12, 1))
                
                addView(primaryButton(txt("Salva", "Save")) {
                    hideKeyboard()
                    onSave(inputs.map { it.text.toString() })
                    dialog.dismiss()
                }, weightParams())
            })
        }

        dialog.setView(dialogView)
        dialog.show()
        
        // Make dialog rounded
        dialog.window?.setBackgroundDrawable(roundedBackground(palette.background, dp(24)))
    }

    private fun showUndoSnackbar(message: String, onUndo: () -> Unit) {
        snackbarContainer.removeAllViews()
        val snackbar = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(16), dp(8), dp(8), dp(8))
            background = roundedBackground("#323232", dp(12))
            elevation = dp(6).toFloat()
            
            addView(TextView(context).apply {
                text = message
                setTextColor(Color.WHITE)
                textSize = 14f
            }, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
            
            addView(Button(context).apply {
                text = txt("Annulla", "Undo")
                setTextColor(Color.parseColor("#FFD600"))
                background = null
                setAllCaps(false)
                setOnClickListener {
                    onUndo()
                    snackbarContainer.removeAllViews()
                }
            })
        }
        
        snackbarContainer.addView(snackbar)
        snackbar.alpha = 0f
        snackbar.translationY = dp(20).toFloat()
        snackbar.animate().alpha(1f).translationY(0f).setDuration(300).start()
        
        snackbarContainer.postDelayed({
            if (snackbarContainer.indexOfChild(snackbar) != -1) {
                snackbar.animate().alpha(0f).translationY(dp(20).toFloat()).setDuration(300)
                    .withEndAction { snackbarContainer.removeView(snackbar) }
                    .start()
            }
        }, 4000)
    }

    private fun exportToCSV() {
        val csv = StringBuilder("Date,Time,Name,Grams,Kcal,Protein,Carbs,Fat\n")
        entryDates.sorted().forEach { date ->
            loadEntries(date).forEach { entry ->
                csv.append("$date,${entry.loggedAt},${entry.name},${entry.grams},${entry.calories},${entry.protein},${entry.carbs},${entry.fat}\n")
            }
        }

        try {
            val file = File(cacheDir, "macro_tracker_export.csv")
            file.writeText(csv.toString())
            val uri = FileProvider.getUriForFile(this, "$packageName.fileprovider", file)
            
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/csv"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            startActivity(Intent.createChooser(intent, txt("Condividi report", "Share report")))
        } catch (e: Exception) {
            AlertDialog.Builder(this)
                .setTitle("Error")
                .setMessage(e.message)
                .show()
        }
    }

    private fun addEntryToDay(entry: FoodEntry) {
        entries.add(entry)
        saveEntriesForCurrentDay()
    }

    private fun copyFromYesterday() {
        val yesterday = currentDate.minusDays(1).toString()
        val yesterdayEntries = loadEntries(yesterday)
        if (yesterdayEntries.isEmpty()) {
            AlertDialog.Builder(this)
                .setTitle(txt("Nulla da copiare", "Nothing to copy"))
                .setMessage(txt("Non ci sono alimenti registrati ieri.", "No foods were logged yesterday."))
                .setPositiveButton("Ok", null)
                .show()
            return
        }
        
        // Ensure relative order is kept but IDs/instances are handled correctly if needed
        // Here we just add them.
        entries.addAll(yesterdayEntries)
        saveEntriesForCurrentDay()
        render()
    }

    private fun getFrequentFoods(): List<FoodEntry> = emptyList() // Section removed

    private fun saveFoodInLibrary(entry: FoodEntry) {
        foodLibrary.removeAll { it.name.equals(entry.name, ignoreCase = true) }
        foodLibrary.add(entry)
        saveFoodLibrary()
    }

    private fun loadData() {
        goals = Goals(
            calories = prefs.getInt("goal_calories", 2200),
            protein = prefs.getInt("goal_protein", 160),
            carbs = prefs.getInt("goal_carbs", 240),
            fat = prefs.getInt("goal_fat", 70),
            weightKg = prefs.getFloat("body_weight", 75f),
            neckCm = prefs.getFloat("body_neck", 0f),
            shouldersCm = prefs.getFloat("body_shoulders", 0f),
            torsoCm = prefs.getFloat("body_torso", 0f),
            chestCm = prefs.getFloat("body_chest", 98f),
            waistCm = prefs.getFloat("body_waist", 82f),
            hipsCm = prefs.getFloat("body_hips", 96f),
            rightBicepsCm = prefs.getFloat("body_right_biceps", 0f),
            leftBicepsCm = prefs.getFloat("body_left_biceps", 0f),
            rightForearmCm = prefs.getFloat("body_right_forearm", 0f),
            leftForearmCm = prefs.getFloat("body_left_forearm", 0f),
            rightWristCm = prefs.getFloat("body_right_wrist", 0f),
            leftWristCm = prefs.getFloat("body_left_wrist", 0f),
            rightThighCm = prefs.getFloat("body_right_thigh", 0f),
            leftThighCm = prefs.getFloat("body_left_thigh", 0f),
            rightCalfCm = prefs.getFloat("body_right_calf", 0f),
            leftCalfCm = prefs.getFloat("body_left_calf", 0f),
            rightAnkleCm = prefs.getFloat("body_right_ankle", 0f),
            leftAnkleCm = prefs.getFloat("body_left_ankle", 0f)
        )
        useEnglish = prefs.getBoolean("use_english", false)
        paletteIndex = prefs.getInt("palette_index", 0).coerceIn(0, palettes.lastIndex)
        entryDates.clear()
        entryDates.addAll(prefs.getStringSet("entry_dates", emptySet()) ?: emptySet())
        loadFoodLibrary()
        loadDishLibrary()
        loadBodyHistory()

        val today = currentDate.toString()
        entries.replaceWith(loadEntries(today))
        migrateOldEntriesIfNeeded(today)
        if (bodyHistory.isEmpty()) saveBodyRecord()
    }

    private fun migrateOldEntriesIfNeeded(today: String) {
        val oldEntries = prefs.getString("entries", null) ?: return
        if (entries.isNotEmpty()) return
        val migrated = parseEntries(oldEntries)
        if (migrated.isNotEmpty()) {
            entries.replaceWith(migrated)
            saveEntriesForCurrentDay()
            prefs.edit().remove("entries").apply()
        }
    }

    private fun loadEntries(date: String): MutableList<FoodEntry> {
        return parseEntries(prefs.getString("entries_$date", "[]") ?: "[]").toMutableList()
    }

    private fun parseEntries(raw: String): List<FoodEntry> {
        val array = JSONArray(raw)
        return (0 until array.length()).map { FoodEntry.fromJson(array.getJSONObject(it)) }
    }

    private fun saveEntriesForCurrentDay() {
        val date = currentDate.toString()
        entryDates.add(date)
        prefs.edit()
            .putString("entries_$date", JSONArray(entries.map { it.toJson() }).toString())
            .putStringSet("entry_dates", entryDates)
            .apply()
    }

    private fun saveAppSettings() {
        prefs.edit()
            .putBoolean("use_english", useEnglish)
            .putInt("palette_index", paletteIndex)
            .apply()
    }

    private fun enableFullScreen() {
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        window.decorView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_FULLSCREEN or
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
    }

    private fun loadFoodLibrary() {
        foodLibrary.clear()
        val array = JSONArray(prefs.getString("food_library", "[]") ?: "[]")
        for (index in 0 until array.length()) {
            foodLibrary.add(FoodEntry.fromJson(array.getJSONObject(index)))
        }
    }

    private fun saveFoodLibrary() {
        prefs.edit()
            .putString("food_library", JSONArray(foodLibrary.map { it.toJson() }).toString())
            .apply()
    }

    private fun loadDishLibrary() {
        dishLibrary.clear()
        val array = JSONArray(prefs.getString("dish_library", "[]") ?: "[]")
        for (index in 0 until array.length()) {
            dishLibrary.add(Dish.fromJson(array.getJSONObject(index)))
        }
    }

    private fun saveDishLibrary() {
        prefs.edit()
            .putString("dish_library", JSONArray(dishLibrary.map { it.toJson() }).toString())
            .apply()
    }

    private fun dishRow(dish: Dish): View {
        var startX = 0f
        var startY = 0f
        var isSwiping = false
        val touchSlop = ViewConfiguration.get(this).scaledTouchSlop

        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(0, dp(10), 0, dp(10))
            addView(rowTitle(dish.name))
            
            val entry = dish.toFoodEntry()
            addView(macroSummaryText("${entry.grams}g | ${String.format("%.1f", entry.calories)} kcal", 
                entry.protein, entry.carbs, entry.fat))
            
            setOnTouchListener { view, event ->
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        startX = event.rawX
                        startY = event.rawY
                        isSwiping = false
                        false
                    }
                    MotionEvent.ACTION_MOVE -> {
                        val dx = event.rawX - startX
                        val dy = event.rawY - startY
                        if (!isSwiping && abs(dx) > touchSlop && abs(dx) > abs(dy)) {
                            isSwiping = true
                            parent.requestDisallowInterceptTouchEvent(true)
                        }
                        
                        if (isSwiping) {
                            view.translationX = dx
                            true
                        } else {
                            false
                        }
                    }
                    MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                        if (isSwiping) {
                            val dx = event.rawX - startX
                            if (abs(dx) > dp(96)) {
                                val item = dish
                                dishLibrary.remove(dish)
                                saveDishLibrary()
                                
                                view.post { 
                                    render() 
                                    showUndoSnackbar(txt("Piatto rimosso dal database", "Dish removed from database")) {
                                        dishLibrary.add(item)
                                        saveDishLibrary()
                                        render()
                                    }
                                }
                            } else {
                                view.animate()
                                    .translationX(0f)
                                    .setDuration(200)
                                    .start()
                            }
                            isSwiping = false
                            true
                        } else {
                            false
                        }
                    }
                    else -> false
                }
            }
        }
    }

    private fun showDishDialog() {
        val dialog = AlertDialog.Builder(this).create()
        val currentIngredients = mutableListOf<FoodEntry>()
        
        fun createDishView(): View {
            return LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setBackgroundColor(color(palette.background))
                setPadding(dp(24), dp(24), dp(24), dp(24))
                
                addView(TextView(context).apply {
                    text = txt("Nuovo Piatto", "New Dish")
                    textSize = 22f
                    setTextColor(color(palette.text))
                    setTypeface(typeface, android.graphics.Typeface.BOLD)
                    setPadding(0, 0, 0, dp(16))
                })

                val nameInput = EditText(context).apply {
                    hint = txt("Nome piatto", "Dish name")
                    setHintTextColor(if (palette.name == "Midnight" || palette.name == "Amber") Color.WHITE else Color.GRAY)
                    setPadding(dp(12), dp(12), dp(12), dp(12))
                    background = roundedBackground(palette.card, dp(12))
                    setTextColor(color(palette.text))
                    setSingleLine(true)
                }
                addView(nameInput)
                addView(space(1, 16))

                val scroll = ScrollView(context).apply { overScrollMode = View.OVER_SCROLL_NEVER }
                val ingredientsList = LinearLayout(context).apply { orientation = LinearLayout.VERTICAL }
                scroll.addView(ingredientsList)
                addView(scroll, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f))

                val summaryText = TextView(context).apply {
                    textSize = 14f
                    setTextColor(color(palette.text))
                    setPadding(0, dp(12), 0, dp(12))
                }
                addView(summaryText)

                fun updateUI() {
                    ingredientsList.removeAllViews()
                    currentIngredients.forEachIndexed { index, ing ->
                        ingredientsList.addView(LinearLayout(context).apply {
                            orientation = LinearLayout.HORIZONTAL
                            gravity = Gravity.CENTER_VERTICAL
                            setPadding(0, dp(8), 0, dp(8))
                            
                            addView(TextView(context).apply {
                                text = "${ing.name} (${ing.grams}g)"
                                setTextColor(color(palette.text))
                                textSize = 15f
                            }, weightParams())
                            
                            addView(TextView(context).apply {
                                text = " ✕ "
                                setTextColor(Color.RED)
                                setPadding(dp(12), dp(12), dp(12), dp(12))
                                setOnClickListener {
                                    currentIngredients.removeAt(index)
                                    updateUI()
                                }
                            })
                        })
                    }
                    
                    val total = Dish("", currentIngredients).toFoodEntry()
                    summaryText.text = txt(
                        "Totale: ${total.calories.roundToInt()} kcal | P ${total.protein.roundToInt()}g C ${total.carbs.roundToInt()}g G ${total.fat.roundToInt()}g",
                        "Total: ${total.calories.roundToInt()} kcal | P ${total.protein.roundToInt()}g C ${total.carbs.roundToInt()}g F ${total.fat.roundToInt()}g"
                    )
                }

                addView(secondaryButton(txt("+ Aggiungi ingrediente", "+ Add ingredient")) {
                    showLibraryPicker(onSelected = { entry ->
                        showServingDialog(entry, onResult = { scaledEntry ->
                            currentIngredients.add(scaledEntry)
                            updateUI()
                        })
                    })
                })

                addView(LinearLayout(context).apply {
                    orientation = LinearLayout.HORIZONTAL
                    setPadding(0, dp(20), 0, 0)
                    addView(secondaryButton(txt("Annulla", "Cancel")) { dialog.dismiss() }, weightParams())
                    addView(space(12, 1))
                    addView(primaryButton(txt("Salva", "Save")) {
                        val name = nameInput.text.toString()
                        if (name.isNotBlank() && currentIngredients.isNotEmpty()) {
                            dishLibrary.add(Dish(name, currentIngredients))
                            saveDishLibrary()
                            render()
                            dialog.dismiss()
                        }
                    }, weightParams())
                })
                
                updateUI()
            }
        }

        dialog.setView(createDishView())
        dialog.show()
        dialog.window?.setBackgroundDrawable(roundedBackground(palette.background, dp(24)))
    }

    private fun showDishPicker(targetMeal: String) {
        if (dishLibrary.isEmpty()) {
            AlertDialog.Builder(this)
                .setTitle(txt("Nessun piatto", "No dishes"))
                .setMessage(txt("Crea prima un piatto nel tab Alimenti.", "Create a dish in the Foods tab first."))
                .setPositiveButton("Ok", null)
                .show()
            return
        }

        val dialog = AlertDialog.Builder(this).create()
        
        fun createPickerView(): View {
            return LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setBackgroundColor(color(palette.background))
                setPadding(dp(24), dp(24), dp(24), dp(24))
                
                addView(TextView(context).apply {
                    text = txt("Aggiungi piatto", "Add dish")
                    textSize = 22f
                    setTextColor(color(palette.text))
                    setTypeface(typeface, android.graphics.Typeface.BOLD)
                    setPadding(0, 0, 0, dp(16))
                })

                val scroll = ScrollView(context).apply { overScrollMode = View.OVER_SCROLL_NEVER }
                val listContainer = LinearLayout(context).apply { orientation = LinearLayout.VERTICAL }
                scroll.addView(listContainer)
                addView(scroll, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f))
                
                dishLibrary.sortedBy { it.name.lowercase() }.forEach { dish ->
                    listContainer.addView(LinearLayout(context).apply {
                        orientation = LinearLayout.VERTICAL
                        setPadding(0, dp(12), 0, dp(12))
                        background = roundedBackground(palette.background, 0)
                        addView(rowTitle(dish.name))
                        val entry = dish.toFoodEntry()
                        addView(macroSummaryText("${entry.grams}g | ${String.format("%.1f", entry.calories)} kcal", 
                            entry.protein, entry.carbs, entry.fat))

                        setOnClickListener {
                            showServingDialog(entry, targetMeal)
                            dialog.dismiss()
                        }
                    })
                }

                addView(secondaryButton(txt("Chiudi", "Close")) { dialog.dismiss() }.apply {
                    val params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(46))
                    params.setMargins(0, dp(16), 0, 0)
                    layoutParams = params
                })
            }
        }

        dialog.setView(createPickerView())
        dialog.show()
        dialog.window?.setBackgroundDrawable(roundedBackground(palette.background, dp(24)))
    }

    private fun loadBodyHistory() {
        bodyHistory.clear()
        val array = JSONArray(prefs.getString("body_history", "[]") ?: "[]")
        for (index in 0 until array.length()) {
            bodyHistory.add(BodyRecord.fromJson(array.getJSONObject(index)))
        }
    }

    private fun saveBodyRecord() {
        val today = LocalDate.now().toString()
        bodyHistory.removeAll { it.date == today }
        bodyHistory.add(
            BodyRecord(
                date = today,
                weightKg = goals.weightKg,
                neckCm = goals.neckCm,
                shouldersCm = goals.shouldersCm,
                torsoCm = goals.torsoCm,
                chestCm = goals.chestCm,
                waistCm = goals.waistCm,
                hipsCm = goals.hipsCm,
                rightBicepsCm = goals.rightBicepsCm,
                leftBicepsCm = goals.leftBicepsCm,
                rightForearmCm = goals.rightForearmCm,
                leftForearmCm = goals.leftForearmCm,
                rightWristCm = goals.rightWristCm,
                leftWristCm = goals.leftWristCm,
                rightThighCm = goals.rightThighCm,
                leftThighCm = goals.leftThighCm,
                rightCalfCm = goals.rightCalfCm,
                leftCalfCm = goals.leftCalfCm,
                rightAnkleCm = goals.rightAnkleCm,
                leftAnkleCm = goals.leftAnkleCm
            )
        )
        prefs.edit()
            .putString("body_history", JSONArray(bodyHistory.sortedBy { it.date }.map { it.toJson() }).toString())
            .apply()
    }

    private fun saveGoals() {
        prefs.edit()
            .putInt("goal_calories", goals.calories)
            .putInt("goal_protein", goals.protein)
            .putInt("goal_carbs", goals.carbs)
            .putInt("goal_fat", goals.fat)
            .putFloat("body_weight", goals.weightKg)
            .putFloat("body_neck", goals.neckCm)
            .putFloat("body_shoulders", goals.shouldersCm)
            .putFloat("body_torso", goals.torsoCm)
            .putFloat("body_chest", goals.chestCm)
            .putFloat("body_waist", goals.waistCm)
            .putFloat("body_hips", goals.hipsCm)
            .putFloat("body_right_biceps", goals.rightBicepsCm)
            .putFloat("body_left_biceps", goals.leftBicepsCm)
            .putFloat("body_right_forearm", goals.rightForearmCm)
            .putFloat("body_left_forearm", goals.leftForearmCm)
            .putFloat("body_right_wrist", goals.rightWristCm)
            .putFloat("body_left_wrist", goals.leftWristCm)
            .putFloat("body_right_thigh", goals.rightThighCm)
            .putFloat("body_left_thigh", goals.leftThighCm)
            .putFloat("body_right_calf", goals.rightCalfCm)
            .putFloat("body_left_calf", goals.leftCalfCm)
            .putFloat("body_right_ankle", goals.rightAnkleCm)
            .putFloat("body_left_ankle", goals.leftAnkleCm)
            .apply()
    }

    private fun totals(): FoodEntry = FoodEntry(
        name = txt("Totale", "Total"),
        calories = entries.sumOf { it.calories },
        protein = entries.sumOf { it.protein },
        carbs = entries.sumOf { it.carbs },
        fat = entries.sumOf { it.fat }
    )

    private fun appHeader(): View {
        val state = when {
            currentDate.isBefore(LocalDate.now()) -> txt("Storico", "History")
            currentDate.isAfter(LocalDate.now()) -> txt("Pianificazione", "Planning")
            else -> txt("Oggi", "Today")
        }
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(0, dp(4), 0, dp(6))
            addView(TextView(context).apply {
                text = "MacroTracker"
                textSize = 31f
                setTextColor(color(palette.text))
                setTypeface(typeface, android.graphics.Typeface.BOLD)
            })
            addView(TextView(context).apply {
                text = "$state | ${currentDate.format(dayFormatter)}"
                textSize = 15f
                setTextColor(color(palette.muted))
            })
        }
    }

    private fun panel(children: LinearLayout.() -> Unit): View {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(18), dp(16), dp(18), dp(16))
            background = roundedBackground(palette.card, dp(18))
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            params.setMargins(0, dp(8), 0, dp(14))
            layoutParams = params
            children()
        }
    }

    private fun accentPanel(children: LinearLayout.() -> Unit): View {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(18), dp(20), dp(18))
            background = roundedBackground(palette.primary, dp(22))
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            params.setMargins(0, dp(8), 0, dp(14))
            layoutParams = params
            children()
        }
    }

    private fun chart(values: List<Int>, target: Int): View {
        return panel {
            addView(BarChartView(context).apply {
                data = values
                marker = target
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    dp(150)
                )
            })
        }
    }

    private fun lineChart(values: List<Int>): View {
        return panel {
            addView(LineChartView(context).apply {
                data = values
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    dp(160)
                )
            })
        }
    }

    private fun title(text: String): View = TextView(this).apply {
        this.text = text
        textSize = 30f
        setTextColor(color(palette.text))
        setTypeface(typeface, android.graphics.Typeface.BOLD)
    }

    private fun subtitle(text: String): View = TextView(this).apply {
        this.text = text
        textSize = 15f
        setTextColor(color(palette.muted))
    }

    private fun sectionTitle(text: String): View = TextView(this).apply {
        this.text = text
        textSize = 19f
        setTextColor(color(palette.text))
        setTypeface(typeface, android.graphics.Typeface.BOLD)
        setPadding(0, dp(12), 0, dp(2))
    }

    private fun rowTitle(text: String): View = TextView(this).apply {
        this.text = text
        textSize = 17f
        setTextColor(color(palette.text))
        setTypeface(typeface, android.graphics.Typeface.BOLD)
    }

    private fun label(left: String, right: String): View = LinearLayout(this).apply {
        orientation = LinearLayout.HORIZONTAL
        addView(bodyText(left), weightParams())
        addView(bodyText(right).apply {
            gravity = Gravity.END
            setTypeface(typeface, android.graphics.Typeface.BOLD)
        }, weightParams())
    }
    private fun foodLogHeader(time: String, foodName: String): View = LinearLayout(this).apply {
        orientation = LinearLayout.HORIZONTAL
        addView(TextView(context).apply {
            text = time
            textSize = 15f
            setTextColor(color(palette.text))
            setTypeface(typeface, android.graphics.Typeface.BOLD)
        }, weightParams())
        addView(TextView(context).apply {
            text = foodName
            textSize = 15f
            gravity = Gravity.END
            setTextColor(color(palette.text))
            setTypeface(typeface, android.graphics.Typeface.BOLD)
        }, weightParams())
    }

    private fun settingRow(label: String, value: String): View = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
        setPadding(0, dp(6), 0, dp(8))
        addView(fieldLabel(label))
        addView(TextView(context).apply {
            text = value
            textSize = 18f
            setTextColor(color(palette.text))
            setTypeface(typeface, android.graphics.Typeface.BOLD)
        })
    }
    private fun macroSettingRow(label: String, value: String, macroColor: Int): View = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
        setPadding(0, dp(6), 0, dp(8))
        addView(fieldLabel(label).apply { setTextColor(macroColor) })
        addView(TextView(context).apply {
            text = value
            textSize = 18f
            setTextColor(macroColor)
            setTypeface(typeface, android.graphics.Typeface.BOLD)
        })
    }

    private fun fieldLabel(textValue: String): TextView = TextView(this).apply {
        text = textValue
        textSize = 13f
        setTextColor(color(palette.muted))
        setPadding(0, dp(8), 0, 0)
    }

    private fun bodyText(textValue: String): TextView = TextView(this).apply {
        text = textValue
        textSize = 15f
        setTextColor(color(palette.muted))
        setPadding(0, dp(2), 0, dp(2))
    }
    private fun macroSummaryText(prefix: String, protein: Double, carbs: Double, fat: Double): TextView {
        return TextView(this).apply {
            textSize = 15f
            setTextColor(color(palette.muted))
            setPadding(0, dp(2), 0, dp(2))
            text = SpannableStringBuilder().apply {
                append(prefix)
                append(" | ")
                appendMacroToken("P ${String.format("%.1f", protein)}g", proteinColor)
                append("  ")
                appendMacroToken("C ${String.format("%.1f", carbs)}g", carbsColor)
                append("  ")
                appendMacroToken("G ${String.format("%.1f", fat)}g", fatColor)
            }
        }
    }
    private fun macroColorForLabel(label: String): Int? {
        val normalized = label.lowercase()
        return when {
            normalized.contains("proteine") || normalized.contains("protein") -> proteinColor
            normalized.contains("carboidrati") || normalized.contains("carbs") -> carbsColor
            normalized.contains("grassi") || normalized.contains("fat") -> fatColor
            else -> null
        }
    }
    private fun SpannableStringBuilder.appendMacroToken(value: String, tokenColor: Int) {
        val start = length
        append(value)
        setSpan(ForegroundColorSpan(tokenColor), start, length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
    }

    private fun progress(value: Int, maxValue: Int): ProgressBar {
        return ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal).apply {
            max = maxValue.coerceAtLeast(1)
            progress = value.coerceAtMost(max)
            setPadding(0, dp(8), 0, dp(4))
        }
    }

    private fun primaryButton(text: String, onClick: () -> Unit): Button {
        return button(text, palette.primary, Color.WHITE, onClick)
    }

    private fun secondaryButton(text: String, onClick: () -> Unit): Button {
        return button(text, palette.secondary, color(palette.text), onClick)
    }

    private fun toggleButton(text: String, active: Boolean, onClick: () -> Unit): Button {
        return button(
            text,
            if (active) palette.primary else palette.secondary,
            if (active) Color.WHITE else color(palette.muted),
            onClick
        ).apply {
            minHeight = dp(38)
            textSize = 13f
        }
    }

    private fun button(textValue: String, background: String, textColor: Int, onClick: () -> Unit): Button {
        return Button(this).apply {
            text = textValue
            setTextColor(textColor)
            textSize = 14f
            setAllCaps(false)
            minHeight = dp(46)
            setPadding(dp(12), 0, dp(12), 0)
            this.background = roundedBackground(background, dp(16))
            elevation = 0f
            stateListAnimator = null
            setOnClickListener { onClick() }
        }
    }

    private fun space(width: Int, height: Int = 1): View = View(this).apply {
        layoutParams = LinearLayout.LayoutParams(dp(width), dp(height))
    }

    private fun weightParams(): LinearLayout.LayoutParams {
        return LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
    }

    private fun formatDate(raw: String): String {
        return runCatching { LocalDate.parse(raw).format(dayFormatter) }.getOrDefault(raw)
    }

    private fun txt(it: String, en: String): String = if (useEnglish) en else it

    private fun currentTimeText(): String = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"))

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).roundToInt()

    private fun color(value: String): Int = Color.parseColor(value)

    private fun roundedBackground(value: String, radius: Int): GradientDrawable {
        return GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = radius.toFloat()
            setColor(color(value))
        }
    }

    private fun Float.format(): String {
        return if (this % 1f == 0f) this.roundToInt().toString() else String.format("%.1f", this)
    }

    private fun String.toDoubleValue(default: Double = 0.0): Double {
        return replace(",", ".").toDoubleOrNull() ?: default
    }

    private fun String.toIntValue(default: Int = 0): Int {
        return replace(",", ".").toFloatOrNull()?.roundToInt() ?: default
    }

    private fun String.toFloatValue(default: Float): Float {
        return replace(",", ".").toFloatOrNull() ?: default
    }

    private fun String.toTimeValue(default: String): String {
        val cleaned = trim().replace(".", ":")
        return runCatching {
            LocalTime.parse(cleaned, DateTimeFormatter.ofPattern("H:mm"))
                .format(DateTimeFormatter.ofPattern("HH:mm"))
        }.getOrDefault(default)
    }

    private fun MutableList<FoodEntry>.replaceWith(items: List<FoodEntry>) {
        clear()
        addAll(items)
    }

    private fun hideKeyboard() {
        val manager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        manager.hideSoftInputFromWindow(root.windowToken, 0)
    }
}

class PieChartView(context: Context) : View(context) {
    var protein: Double = 0.0
    var carbs: Double = 0.0
    var fat: Double = 0.0

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    private val emptyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { 
        style = Paint.Style.STROKE
        strokeWidth = 4f
        color = Color.LTGRAY 
    }
    private val rect = android.graphics.RectF()

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val size = min(width, height).toFloat()
        rect.set(2f, 2f, size - 2f, size - 2f)
        
        val pCal = protein * 4
        val cCal = carbs * 4
        val fCal = fat * 9
        val total = pCal + cCal + fCal

        if (total <= 0) {
            canvas.drawCircle(size / 2, size / 2, (size / 2) - 4f, emptyPaint)
            return
        }

        var startAngle = -90f
        
        // Protein (Red)
        val pSweep = (pCal / total).toFloat() * 360f
        paint.color = PROTEIN_COLOR
        canvas.drawArc(rect, startAngle, pSweep, true, paint)
        startAngle += pSweep

        // Carbs (Blue)
        val cSweep = (cCal / total).toFloat() * 360f
        paint.color = CARBS_COLOR
        canvas.drawArc(rect, startAngle, cSweep, true, paint)
        startAngle += cSweep

        // Fat (Yellow)
        val fSweep = (360f - pSweep - cSweep).coerceAtLeast(0f)
        paint.color = FAT_COLOR
        canvas.drawArc(rect, startAngle, fSweep, true, paint)
    }
}

class BarChartView(context: Context) : View(context) {
    var data: List<Int> = emptyList()
    var marker: Int = 0

    private val barPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#2E7D64") }
    private val markerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#D05F3F")
        strokeWidth = 3f
    }
    private val axisPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#D9D3CA")
        strokeWidth = 2f
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (data.isEmpty()) return

        val maxValue = max(data.maxOrNull() ?: 1, marker).coerceAtLeast(1)
        val chartBottom = height - 12f
        val chartTop = 12f
        val chartHeight = chartBottom - chartTop
        val slotWidth = width.toFloat() / data.size

        canvas.drawLine(0f, chartBottom, width.toFloat(), chartBottom, axisPaint)

        data.forEachIndexed { index, value ->
            val barHeight = (value.toFloat() / maxValue) * chartHeight
            val left = index * slotWidth + slotWidth * 0.18f
            val right = (index + 1) * slotWidth - slotWidth * 0.18f
            canvas.drawRoundRect(left, chartBottom - barHeight, right, chartBottom, 8f, 8f, barPaint)
        }

        if (marker > 0) {
            val y = chartBottom - (marker.toFloat() / maxValue) * chartHeight
            canvas.drawLine(0f, y, width.toFloat(), y, markerPaint)
        }
    }
}

class LineChartView(context: Context) : View(context) {
    var data: List<Int> = emptyList()

    private val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#2E7D64")
        strokeWidth = 5f
        strokeCap = Paint.Cap.ROUND
    }
    private val pointPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#2E7D64")
        style = Paint.Style.FILL
    }
    private val axisPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#D9D3CA")
        strokeWidth = 2f
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (data.isEmpty()) return

        val leftPadding = 10f
        val rightPadding = 10f
        val topPadding = 16f
        val bottomPadding = 18f
        val chartLeft = leftPadding
        val chartRight = width - rightPadding
        val chartTop = topPadding
        val chartBottom = height - bottomPadding
        val chartHeight = chartBottom - chartTop
        val minValue = data.minOrNull() ?: 0
        val maxValue = data.maxOrNull() ?: 1
        val range = (maxValue - minValue).coerceAtLeast(1)

        canvas.drawLine(chartLeft, chartBottom, chartRight, chartBottom, axisPaint)

        fun xFor(index: Int): Float {
            if (data.size == 1) return (chartLeft + chartRight) / 2f
            return chartLeft + (index.toFloat() / (data.size - 1)) * (chartRight - chartLeft)
        }

        fun yFor(value: Int): Float {
            return chartBottom - ((value - minValue).toFloat() / range) * chartHeight
        }

        data.forEachIndexed { index, value ->
            val x = xFor(index)
            val y = yFor(value)
            if (index > 0) {
                val previousX = xFor(index - 1)
                val previousY = yFor(data[index - 1])
                canvas.drawLine(previousX, previousY, x, y, linePaint)
            }
            canvas.drawCircle(x, y, 7f, pointPaint)
        }
    }
}
