extends CharacterBody2D
class_name Echo

signal echo_destroyed

const SPEED = 280.0
const JUMP_VELOCITY = -450.0
const JUMP_CUT_VELOCITY = -140.0
const GRAVITY = 1100.0
const ACCEL_GROUND = 2200.0
const ACCEL_AIR = 1650.0
const FRICTION_GROUND = 1800.0
const FRICTION_AIR = 600.0

var recorded_inputs: Array = []
var current_frame: int = 0
var stays_at_end: bool = true
var is_finished: bool = false
var facing_right: bool = true

@onready var visual_sprite: Node2D = $Visual
@onready var collision_shape: CollisionShape2D = $CollisionShape2D

func _ready() -> void:
	add_to_group("echo")
	add_to_group("bodies_interact")
	if visual_sprite:
		visual_sprite.modulate = Color(0.22, 0.88, 1.0, 0.55)

func initialize(inputs: Array, spawn_pos: Vector2, keep_at_end: bool = true) -> void:
	recorded_inputs = inputs.duplicate(true)
	global_position = spawn_pos
	stays_at_end = keep_at_end
	current_frame = 0
	is_finished = false
	velocity = Vector2.ZERO

func _physics_process(delta: float) -> void:
	var input_left = false
	var input_right = false
	var input_jump = false

	if current_frame < recorded_inputs.size():
		var frame_data = recorded_inputs[current_frame]
		input_left = frame_data.get("left", false)
		input_right = frame_data.get("right", false)
		input_jump = frame_data.get("jump", false)
		current_frame += 1
	else:
		is_finished = true
		if not stays_at_end:
			queue_free()
			return

	# Movimento Horizontal Idêntico
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

	# Gravidade e Salto
	if not is_on_floor():
		velocity.y += GRAVITY * delta

	if input_jump and is_on_floor():
		velocity.y = JUMP_VELOCITY

	if not input_jump and velocity.y < JUMP_CUT_VELOCITY:
		velocity.y = JUMP_CUT_VELOCITY

	move_and_slide()

	if visual_sprite:
		visual_sprite.scale.x = 1.0 if facing_right else -1.0

func destroy() -> void:
	echo_destroyed.emit()
	queue_free()
