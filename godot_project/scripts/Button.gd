extends Area2D
class_name PressureButton

signal button_pressed
signal button_released
signal state_changed(is_active: bool)

@export var button_id: int = 1
@export var is_timed: bool = false
@export var timed_duration: float = 2.0

var is_pressed: bool = false
var timer_remaining: float = 0.0
var overlapping_bodies: Array = []

@onready var visual_plate: Node2D = $Visual
@onready var timer_gauge: ProgressBar = $TimerGauge

func _ready() -> void:
	body_entered.connect(_on_body_entered)
	body_exited.connect(_on_body_exited)
	update_visuals()
	if timer_gauge:
		timer_gauge.visible = is_timed
		timer_gauge.max_value = timed_duration
		timer_gauge.value = 0.0

func _physics_process(delta: float) -> void:
	var was_active = is_active()

	if overlapping_bodies.size() > 0:
		is_pressed = true
		if is_timed:
			timer_remaining = timed_duration
	else:
		is_pressed = false
		if is_timed and timer_remaining > 0.0:
			timer_remaining = max(0.0, timer_remaining - delta)

	if timer_gauge and is_timed:
		timer_gauge.value = timer_remaining
		timer_gauge.visible = timer_remaining > 0.0

	var now_active = is_active()
	if now_active != was_active:
		state_changed.emit(now_active)
		if now_active:
			button_pressed.emit()
		else:
			button_released.emit()
		update_visuals()

func is_active() -> bool:
	return is_pressed or timer_remaining > 0.0

func _on_body_entered(body: Node2D) -> void:
	if body.is_in_group("player") or body.is_in_group("echo") or body.is_in_group("bodies_interact"):
		if not overlapping_bodies.has(body):
			overlapping_bodies.append(body)

func _on_body_exited(body: Node2D) -> void:
	if overlapping_bodies.has(body):
		overlapping_bodies.erase(body)

func update_visuals() -> void:
	if visual_plate:
		if is_active():
			visual_plate.modulate = Color(1.0, 0.84, 0.0) if is_timed else Color(0.0, 0.9, 0.46)
		else:
			visual_plate.modulate = Color(0.5, 0.42, 0.0) if is_timed else Color(0.0, 0.45, 0.23)
