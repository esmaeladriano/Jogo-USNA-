extends Control
class_name VirtualControls

# Controlos táteis virtuais com suporte multitoque e respeito por safe area

@onready var btn_left: TouchScreenButton = $LeftControls/BtnLeft
@onready var btn_right: TouchScreenButton = $LeftControls/BtnRight
@onready var btn_jump: TouchScreenButton = $RightControls/BtnJump
@onready var btn_record: TouchScreenButton = $RightControls/BtnRecord
@onready var btn_release: TouchScreenButton = $RightControls/BtnRelease
@onready var safe_area_container: MarginContainer = $SafeAreaContainer

func _ready() -> void:
	apply_safe_area()
	get_tree().root.size_changed.connect(apply_safe_area)

func apply_safe_area() -> void:
	var safe_area = DisplayServer.get_display_safe_area()
	var window_size = DisplayServer.window_get_size()

	if window_size.x > 0 and window_size.y > 0 and safe_area_container:
		var left_margin = safe_area.position.x
		var top_margin = safe_area.position.y
		var right_margin = window_size.x - (safe_area.position.x + safe_area.size.x)
		var bottom_margin = window_size.y - (safe_area.position.y + safe_area.size.y)

		safe_area_container.add_theme_constant_override("margin_left", int(left_margin))
		safe_area_container.add_theme_constant_override("margin_top", int(top_margin))
		safe_area_container.add_theme_constant_override("margin_right", int(right_margin))
		safe_area_container.add_theme_constant_override("margin_bottom", int(bottom_margin))
