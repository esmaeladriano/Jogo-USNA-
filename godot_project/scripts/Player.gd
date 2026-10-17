extends CharacterBody2D
class_name Player

# Sinais
signal jump_triggered
signal record_started
signal record_ended
signal player_died

# Constantes de Física (60 Hz)
const SPEED = 280.0
const JUMP_VELOCITY = -450.0
const JUMP_CUT_VELOCITY = -140.0
const GRAVITY = 1100.0
const ACCEL_GROUND = 2200.0
const ACCEL_AIR = 1650.0
const FRICTION_GROUND = 1800.0
const FRICTION_AIR = 600.0

const COYOTE_TIME = 0.1 # 6 ticks a 60 FPS
const JUMP_BUFFER_TIME = 0.1

const MAX_RECORD_SECONDS = 3.0
const PHYSICS_TICKS_PER_SEC = 60
const MAX_RECORD_TICKS = int(MAX_RECORD_SECONDS * PHYSICS_TICKS_PER_SEC)

# Timers de Movimento
var coyote_timer: float = 0.0
var jump_buffer_timer: float = 0.0
var facing_right: bool = true

# Sistema de Gravação
var is_recording: bool = false
var recorded_inputs: Array = []
var record_start_position: Vector2 = Vector2.ZERO
var has_pending_recording: bool = false

# Estado do Jogador
var is_dead: bool = false
var death_timer: float = 0.0

@onready var visual_sprite: Node2D = $Visual
@onready var collision_shape: CollisionShape2D = $CollisionShape2D

func _ready() -> void:
	add_to_group("player")

func _physics_process(delta: float) -> void:
	if is_dead:
		death_timer += delta
		if death_timer >= 0.5:
			get_tree().call_group("level_manager", "restart_level")
		return

	# 1. Obter Inputs
	var input_left = Input.is_action_pressed("move_left")
	var input_right = Input.is_action_pressed("move_right")
	var input_jump = Input.is_action_pressed("jump")
	var input_jump_just = Input.is_action_just_pressed("jump")
	var input_jump_released = Input.is_action_just_released("jump")

	# Registo frame a frame (determinístico a 60 Hz)
	if is_recording:
		var frame_data = {
			"left": input_left,
			"right": input_right,
			"jump": input_jump
		}
		recorded_inputs.append(frame_data)
		if recorded_inputs.size() >= MAX_RECORD_TICKS:
			stop_recording()

	# 2. Gestão de Coyote Time e Jump Buffer
	if is_on_floor():
		coyote_timer = COYOTE_TIME
	else:
		coyote_timer = max(0.0, coyote_timer - delta)

	if input_jump_just:
		jump_buffer_timer = JUMP_BUFFER_TIME
	else:
		jump_buffer_timer = max(0.0, jump_buffer_timer - delta)

	# 3. Movimento Horizontal
	var direction = 0.0
	if input_right and not input_left:
		direction = 1.0
		facing_right = true
	elif input_left and not input_right:
		direction = -1.0
		facing_right = false

	var target_speed = direction * SPEED
	var accel = ACCEL_GROUND if is_on_floor() else ACCEL_AIR
	var friction = FRICTION_GROUND if is_on_floor() else FRICTION_AIR

	if direction != 0.0:
		velocity.x = move_toward(velocity.x, target_speed, accel * delta)
	else:
		velocity.x = move_toward(velocity.x, 0.0, friction * delta)

	# 4. Gravidade e Salto
	if not is_on_floor():
		velocity.y += GRAVITY * delta

	# Execução de Salto (Jump Buffer + Coyote Time)
	if jump_buffer_timer > 0.0 and coyote_timer > 0.0:
		velocity.y = JUMP_VELOCITY
		jump_buffer_timer = 0.0
		coyote_timer = 0.0
		jump_triggered.emit()

	# Altura de Salto Variável (soltar botão corta o salto)
	if input_jump_released and velocity.y < JUMP_CUT_VELOCITY:
		velocity.y = JUMP_CUT_VELOCITY

	# Aplicar física
	move_and_slide()

	# Atualizar orientação visual
	if visual_sprite:
		visual_sprite.scale.x = 1.0 if facing_right else -1.0

func start_recording() -> void:
	if is_dead or is_recording:
		return
	is_recording = true
	recorded_inputs.clear()
	record_start_position = global_position
	record_started.emit()

func stop_recording() -> void:
	if not is_recording:
		return
	is_recording = false
	has_pending_recording = recorded_inputs.size() > 0
	record_ended.emit()

func die() -> void:
	if is_dead:
		return
	is_dead = true
	death_timer = 0.0
	velocity = Vector2.ZERO
	is_recording = false
	recorded_inputs.clear()
	has_pending_recording = false
	player_died.emit()
	if visual_sprite:
		visual_sprite.modulate = Color(1.0, 0.2, 0.2, 0.5)
