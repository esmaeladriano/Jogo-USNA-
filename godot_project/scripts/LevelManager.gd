extends Node2D
class_name LevelManager

@export var current_level_id: int = 1
@export var max_echoes: int = 3
@export var echo_scene: PackedScene

var player: Player
var active_echoes: Array = []
var level_time: float = 0.0
var echoes_used: int = 0
var is_level_won: bool = false

func _ready() -> void:
	add_to_group("level_manager")
	player = get_tree().get_first_node_in_group("player") as Player
	if not echo_scene:
		echo_scene = preload("res://scenes/Echo.tscn")

func _process(delta: float) -> void:
	if not is_level_won and player and not player.is_dead:
		level_time += delta

	# Teclado desktop para testes
	if Input.is_action_just_pressed("record"):
		toggle_recording()
	if Input.is_action_just_pressed("release"):
		release_echo()
	if Input.is_action_just_pressed("restart"):
		restart_level()

func toggle_recording() -> void:
	if not player:
		return
	if player.is_recording:
		player.stop_recording()
	else:
		if active_echoes.size() < max_echoes:
			player.start_recording()

func release_echo() -> void:
	if not player or not player.has_pending_recording:
		return
	if active_echoes.size() >= max_echoes:
		return

	var echo = echo_scene.instantiate() as Echo
	add_child(echo)
	echo.initialize(player.recorded_inputs, player.record_start_position, true)
	active_echoes.append(echo)
	echoes_used += 1

	player.has_pending_recording = false
	player.recorded_inputs.clear()

	echo.echo_destroyed.connect(func(): active_echoes.erase(echo))

func restart_level() -> void:
	for echo in active_echoes:
		if is_instance_valid(echo):
			echo.queue_free()
	active_echoes.clear()
	level_time = 0.0
	echoes_used = 0
	is_level_won = false

	get_tree().reload_current_scene()

func on_portal_reached() -> void:
	if is_level_won:
		return
	is_level_won = true
	var stars = 1
	if level_time <= 20.0 and echoes_used <= max_echoes:
		stars = 3
	elif level_time <= 30.0:
		stars = 2

	GameData.complete_level(current_level_id, stars, level_time)
	# Mostrar diálogo de vitória
	var hud = get_tree().get_first_node_in_group("hud")
	if hud and hud.has_method("show_victory"):
		hud.show_victory(stars, level_time, echoes_used)
