# GameManager.gd — Gestor global de estado do jogo TRILHO
extends Node

signal game_started
signal game_over(distance_meters: int, coins_earned: int, is_new_record: bool)
signal score_updated(distance_meters: int, coins: int, crystals: int)
signal power_up_activated(type: String, duration: float)
signal power_up_ended(type: String)

var current_distance: float = 0.0
var run_coins: int = 0
var crystals_stock: int = 0
const MAX_CRYSTALS: int = 3

var is_running: bool = false
var is_invulnerable: bool = false
var invulnerable_timer: float = 0.0

var has_shield: bool = false
var magnet_timer: float = 0.0
var boost_timer: float = 0.0

var current_speed: float = 680.0
const BASE_SPEED: float = 680.0
const SPEED_STEP_PER_500M: float = 45.0

func _process(delta: float) -> void:
	if not is_running:
		return
	
	# Boost speed multiplier
	var speed_mult = 1.6 if boost_timer > 0.0 else 1.0
	current_speed = (BASE_SPEED + (int(get_distance_meters() / 500.0) * SPEED_STEP_PER_500M)) * speed_mult
	current_distance += current_speed * delta
	
	# Power-up timers
	if invulnerable_timer > 0.0:
		invulnerable_timer -= delta
		if invulnerable_timer <= 0.0:
			is_invulnerable = false
	
	if magnet_timer > 0.0:
		magnet_timer -= delta
		if magnet_timer <= 0.0:
			power_up_ended.emit("magnet")
	
	if boost_timer > 0.0:
		boost_timer -= delta
		if boost_timer <= 0.0:
			power_up_ended.emit("boost")
			
	score_updated.emit(get_distance_meters(), run_coins, crystals_stock)

func start_game() -> void:
	current_distance = 0.0
	run_coins = 0
	crystals_stock = 0
	is_running = true
	is_invulnerable = false
	invulnerable_timer = 0.0
	has_shield = false
	magnet_timer = 0.0
	boost_timer = 0.0
	current_speed = BASE_SPEED
	game_started.emit()

func end_game() -> void:
	if not is_running:
		return
	is_running = false
	var dist = get_distance_meters()
	SaveManager.add_coins(run_coins)
	var is_new_record = SaveManager.update_best_distance(dist)
	game_over.emit(dist, run_coins, is_new_record)

func get_distance_meters() -> int:
	return int(current_distance / 12.0)

func add_coin() -> void:
	run_coins += 1

func add_crystal() -> void:
	if crystals_stock < MAX_CRYSTALS:
		crystals_stock += 1

func use_crystal_ability() -> bool:
	if crystals_stock > 0 and not is_invulnerable:
		crystals_stock -= 1
		is_invulnerable = true
		invulnerable_timer = 1.5
		return true
	return false

func activate_power_up(type: String) -> void:
	match type:
		"shield":
			has_shield = true
			power_up_activated.emit("shield", 8.0)
		"magnet":
			magnet_timer = 8.0
			power_up_activated.emit("magnet", 8.0)
		"boost":
			boost_timer = 4.0
			is_invulnerable = true
			invulnerable_timer = 4.0
			power_up_activated.emit("boost", 4.0)
