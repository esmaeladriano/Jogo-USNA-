# Player.gd — Personagem nas 3 faixas com interpolação suave, salto e deslize
extends CharacterBody2D

signal jumped
signal slid
signal lane_changed(new_lane: int)

# Coordenadas das faixas X para resolução 720x1280
# Faixa 0 = Esquerda (180), Faixa 1 = Centro (360), Faixa 2 = Direita (540)
const LANE_X = [180.0, 360.0, 540.0]
const BASE_Y = 1040.0

var current_lane: int = 1
var is_jumping: bool = false
var is_sliding: bool = false

var jump_velocity_y: float = 0.0
const JUMP_IMPULSE: float = -650.0
const GRAVITY: float = 1800.0

var slide_timer: float = 0.0
const SLIDE_DURATION: float = 0.6

var tween_lane: Tween

# Detetor de gestos táteis
var touch_start_pos: Vector2 = Vector2.ZERO
var is_touching: bool = false
const SWIPE_THRESHOLD: float = 50.0

@onready var sprite: ColorRect = $ColorRect
@onready var collision_shape: CollisionShape2D = $CollisionShape2D

func _ready() -> void:
	position = Vector2(LANE_X[current_lane], BASE_Y)

func _process(delta: float) -> void:
	# Lógica de Salto com gravidade
	if is_jumping:
		jump_velocity_y += GRAVITY * delta
		position.y += jump_velocity_y * delta
		if position.y >= BASE_Y:
			position.y = BASE_Y
			is_jumping = false
			jump_velocity_y = 0.0
			update_visuals()
	
	# Lógica de Deslize com temporizador
	if is_sliding:
		slide_timer -= delta
		if slide_timer <= 0.0:
			is_sliding = false
			update_visuals()

func _input(event: InputEvent) -> void:
	# 1. Teclado
	if event.is_action_pressed("move_left"):
		change_lane(current_lane - 1)
	elif event.is_action_pressed("move_right"):
		change_lane(current_lane + 1)
	elif event.is_action_pressed("jump"):
		jump()
	elif event.is_action_pressed("slide"):
		slide()
	elif event.is_action_pressed("crystal_ability"):
		GameManager.use_crystal_ability()
	
	# 2. Gestos de Swipe para telemóvel (mínimo 50px)
	if event is InputEventScreenTouch:
		if event.pressed:
			touch_start_pos = event.position
			is_touching = true
		else:
			if is_touching:
				var swipe_vec = event.position - touch_start_pos
				if swipe_vec.length() >= SWIPE_THRESHOLD:
					handle_swipe(swipe_vec)
				is_touching = false

func handle_swipe(swipe: Vector2) -> void:
	if abs(swipe.x) > abs(swipe.y):
		if swipe.x > 0:
			change_lane(current_lane + 1)
		else:
			change_lane(current_lane - 1)
	else:
		if swipe.y < 0:
			jump()
		else:
			slide()

func change_lane(new_lane: int) -> void:
	if new_lane < 0 or new_lane > 2 or new_lane == current_lane:
		return
	current_lane = new_lane
	if tween_lane and tween_lane.is_valid():
		tween_lane.kill()
	tween_lane = create_tween()
	# Interpolação suave em cerca de 0.15s conforme requisito
	tween_lane.tween_property(self, "position:x", LANE_X[current_lane], 0.15).set_trans(Tween.TRANS_QUAD).set_ease(Tween.EASE_OUT)
	lane_changed.emit(current_lane)

func jump() -> void:
	if is_jumping or not GameManager.is_running:
		return
	is_jumping = true
	is_sliding = false
	jump_velocity_y = JUMP_IMPULSE
	jumped.emit()
	update_visuals()

func slide() -> void:
	if not GameManager.is_running:
		return
	# Deslizar no ar acelera descida
	if is_jumping:
		jump_velocity_y = max(jump_velocity_y, 400.0)
	is_sliding = true
	slide_timer = SLIDE_DURATION
	slid.emit()
	update_visuals()

func update_visuals() -> void:
	if is_sliding:
		# Personagem baixa (36px de altura em vez de 72px)
		sprite.size = Vector2(44, 34)
		sprite.position = Vector2(-22, -17)
		if collision_shape.shape is RectangleShape2D:
			collision_shape.shape.size = Vector2(40, 32)
	elif is_jumping:
		sprite.size = Vector2(44, 72)
		sprite.position = Vector2(-22, -72)
		if collision_shape.shape is RectangleShape2D:
			collision_shape.shape.size = Vector2(40, 68)
	else:
		sprite.size = Vector2(44, 72)
		sprite.position = Vector2(-22, -72)
		if collision_shape.shape is RectangleShape2D:
			collision_shape.shape.size = Vector2(40, 68)
