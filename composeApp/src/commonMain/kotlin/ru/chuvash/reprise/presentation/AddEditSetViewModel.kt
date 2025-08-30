@file:Suppress("UNCHECKED_CAST")

package ru.chuvash.reprise.presentation

import DistanceUnit
import Exercise
import ExerciseType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import ru.chuvash.reprise.data.WorkoutRepository
import ru.chuvash.reprise.domain.services.WorkoutService

data class AddEditSetState(
    val isLoading: Boolean = true,
    val isEditMode: Boolean = false,
    val allExercises: List<Exercise> = emptyList(),
    val selectedExercise: Exercise? = null,
    val reps: String = "",
    val weight: String = "",
    val durationHours: String = "",
    val durationMinutes: String = "",
    val durationSeconds: String = "",
    val distance: String = "",
    val distanceUnit: DistanceUnit = DistanceUnit.METERS,
    val effortPoints: Int = 0,
    val isSaveEnabled: Boolean = false,
    val isFinished: Boolean = false
)

class AddEditSetViewModel(
    private val date: LocalDate,
    private val setId: String?,
    private val repository: WorkoutRepository,
    private val workoutService: WorkoutService
) : BaseViewModel() {

    private val _isLoading = MutableStateFlow(true)
    private val _isEditMode = MutableStateFlow(false)
    private val _allExercises = MutableStateFlow<List<Exercise>>(emptyList())
    private val _selectedExercise = MutableStateFlow<Exercise?>(null)
    private val _reps = MutableStateFlow("")
    private val _weight = MutableStateFlow("")
    private val _durationHours = MutableStateFlow<String>("")
    private val _durationMinutes = MutableStateFlow<String>("")
    private val _durationSeconds = MutableStateFlow<String>("")
    private val _distance = MutableStateFlow<String>("")
    private val _distanceUnit = MutableStateFlow<DistanceUnit>(DistanceUnit.METERS)
    private val _effortPoints = MutableStateFlow(0)
    private val _isSaveEnabled = MutableStateFlow(false)
    private val _isFinished = MutableStateFlow(false)

    val uiState: StateFlow<AddEditSetState> = combine(
        _isLoading,
        _isEditMode,
        _allExercises,
        _selectedExercise,
        _reps,
        _weight,
        _durationHours,
        _durationMinutes,
        _durationSeconds,
        _distance,
        _distanceUnit,
        _effortPoints,
        _isSaveEnabled,
        _isFinished,
    ) { flows ->
        AddEditSetState(
            isLoading = flows[0] as Boolean,
            isEditMode = flows[1] as Boolean,
            allExercises = flows[2] as List<Exercise>,
            selectedExercise = flows[3] as Exercise?,
            reps = flows[4] as String,
            weight = flows[5] as String,
            durationHours = flows[6] as String,
            durationMinutes = flows[7] as String,
            durationSeconds = flows[8] as String,
            distance = flows[9] as String,
            distanceUnit = flows[10] as DistanceUnit,
            effortPoints = flows[11] as Int,
            isSaveEnabled = flows[12] as Boolean,
            isFinished = flows[13] as Boolean
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AddEditSetState())

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        viewModelScope.launch(Dispatchers.Default) {
            _isLoading.value = true
            val exercises = repository.getAllExercises()

            if (setId == null) { // Режим создания
                _allExercises.value = exercises
                _selectedExercise.value = exercises.firstOrNull()
                _isEditMode.value = false
                _isLoading.value = false
            } else { // Режим редактирования
                val setToEdit = repository.getSetById(setId)
                if (setToEdit != null) {
                    val totalSeconds = setToEdit.durationSeconds ?: 0
                    val hours = totalSeconds / 3600
                    val minutes = (totalSeconds % 3600) / 60
                    val seconds = totalSeconds % 60

                    val distanceMeters = setToEdit.distanceMeters ?: 0
                    val (displayDistance, displayUnit) = if (distanceMeters >= 1000 && distanceMeters % 1000 == 0) {
                        (distanceMeters / 1000).toString() to DistanceUnit.KILOMETERS
                    } else {
                        distanceMeters.toString() to DistanceUnit.METERS
                    }

                    _allExercises.value = exercises
                    _selectedExercise.value = setToEdit.exercise
                    _reps.value = setToEdit.reps?.toString() ?: ""
                    _weight.value = setToEdit.weight?.toString() ?: ""
                    _durationHours.value = if (hours > 0) hours.toString() else ""
                    _durationMinutes.value = if (minutes > 0) minutes.toString() else ""
                    _durationSeconds.value = if (seconds > 0) seconds.toString() else ""
                    _distance.value = if (distanceMeters > 0) displayDistance else ""
                    _distanceUnit.value = displayUnit
                    _effortPoints.value = setToEdit.effortPoints
                    _isEditMode.value = true
                    _isLoading.value = false
                } else {
                    _isLoading.value = false
                    _isFinished.value = true
                }
            }
            validateInput()
        }
    }

    fun onExerciseSelected(exercise: Exercise) {
        _selectedExercise.value = exercise
        _reps.value = ""
        _weight.value = ""
        _durationHours.value = ""
        _durationMinutes.value = ""
        _durationSeconds.value = ""
        _distance.value = ""

        recalculatePoints()
    }

    fun onRepsChanged(value: String) {
        _reps.value = value.filter { c -> c.isDigit() }
        recalculatePoints()
    }

    fun onWeightChanged(value: String) {
        _weight.value = value
        recalculatePoints()
    }

    fun onDurationHoursChanged(value: String) {
        _durationHours.value = value.filter { c -> c.isDigit() }
        recalculatePoints()
    }

    fun onDurationMinutesChanged(value: String) {
        _durationMinutes.value = value.filter { c -> c.isDigit() }
        recalculatePoints()
    }

    fun onDurationSecondsChanged(value: String) {
        _durationSeconds.value = value.filter { c -> c.isDigit() }
        recalculatePoints()
    }

    fun onDistanceChanged(value: String) {
        _distance.value = value
        recalculatePoints()
    }

    fun onDistanceUnitChanged() {
        val newUnit = when(_distanceUnit.value) {
            DistanceUnit.METERS -> DistanceUnit.KILOMETERS
            DistanceUnit.KILOMETERS -> DistanceUnit.METERS
        }
        _distanceUnit.value = newUnit
        recalculatePoints()
    }

    private fun getTotalDurationInSeconds(): Int {
        val hours = _durationHours.value.toIntOrNull() ?: 0
        val minutes = _durationMinutes.value.toIntOrNull() ?: 0
        val seconds = _durationSeconds.value.toIntOrNull() ?: 0
        return (hours * 3600) + (minutes * 60) + seconds
    }

    private fun getTotalDistanceInMeters(): Int {
        val distanceValue = _distance.value.replace(',', '.').toDoubleOrNull() ?: 0.0
        return when (_distanceUnit.value) {
            DistanceUnit.METERS -> distanceValue.toInt()
            DistanceUnit.KILOMETERS -> (distanceValue * 1000).toInt()
        }
    }

    private fun recalculatePoints() {
        val exercise = _selectedExercise.value ?: return

        val points = workoutService.calculateEffortPoints(
            exercise = exercise,
            reps = _reps.value.toIntOrNull(),
            weight = _weight.value.replace(',', '.').toDoubleOrNull(),
            durationSeconds = getTotalDurationInSeconds(),
            distanceMeters = getTotalDistanceInMeters()
        )
        _effortPoints.value = points
        validateInput()
    }

    private fun validateInput() {

        val exercise = _selectedExercise.value ?: run {
            _isSaveEnabled.value = false;
            return
        }

        val isValid = when (exercise.type) {
            ExerciseType.REPS_ONLY -> (_reps.value.toIntOrNull() ?: 0) > 0
            ExerciseType.REPS_AND_WEIGHT -> ((_reps.value.toIntOrNull() ?: 0) > 0)
                    && ((_weight.value.replace(',', '.').toDoubleOrNull() ?: 0.0) > 0.0)

            ExerciseType.TIME -> getTotalDurationInSeconds() > 0
            ExerciseType.TIME_AND_DISTANCE -> (getTotalDurationInSeconds() > 0) && (getTotalDistanceInMeters() > 0)
        }

        _isSaveEnabled.value = isValid
    }

    fun saveSet() {
        viewModelScope.launch {
            val exercise = _selectedExercise.value ?: return@launch
            val reps = _reps.value.toIntOrNull()
            val weight = _weight.value.replace(',', '.').toDoubleOrNull()
            val duration = getTotalDurationInSeconds()
            val distance = getTotalDistanceInMeters()

            if (_isEditMode.value) {
                repository.updateSet(
                    setId!!,
                    exercise.id,
                    reps,
                    weight,
                    duration,
                    distance,
                    _effortPoints.value
                )
            } else {
                repository.addSet(
                    date,
                    exercise.id,
                    reps,
                    weight,
                    duration,
                    distance,
                    _effortPoints.value
                )
            }

            _isFinished.value = true
        }
    }
}