# SaveManager.gd — Persistência em user:// para TRILHO
extends Node

const SAVE_PATH = "user://trilho_save.json"

var total_coins: int = 0
var best_distance: int = 0
var unlocked_skins: Array = ["default", "speedster"]
var selected_skin: String = "default"
var music_volume: float = 0.8
var sfx_volume: float = 0.9
var vibration_enabled: bool = true
var language: String = "pt"

func _ready() -> void:
	load_data()

func save_data() -> void:
	var data = {
		"total_coins": total_coins,
		"best_distance": best_distance,
		"unlocked_skins": unlocked_skins,
		"selected_skin": selected_skin,
		"music_volume": music_volume,
		"sfx_volume": sfx_volume,
		"vibration_enabled": vibration_enabled,
		"language": language
	}
	var file = FileAccess.open(SAVE_PATH, FileAccess.WRITE)
	if file:
		file.store_string(JSON.stringify(data, "\t"))
		file.close()

func load_data() -> void:
	if not FileAccess.file_exists(SAVE_PATH):
		return
	var file = FileAccess.open(SAVE_PATH, FileAccess.READ)
	if not file:
		return
	var text = file.get_as_text()
	file.close()
	var json = JSON.new()
	if json.parse(text) == OK:
		var data = json.data
		if typeof(data) == TYPE_DICTIONARY:
			total_coins = int(data.get("total_coins", 0))
			best_distance = int(data.get("best_distance", 0))
			unlocked_skins = data.get("unlocked_skins", ["default", "speedster"])
			selected_skin = str(data.get("selected_skin", "default"))
			music_volume = float(data.get("music_volume", 0.8))
			sfx_volume = float(data.get("sfx_volume", 0.9))
			vibration_enabled = bool(data.get("vibration_enabled", true))
			language = str(data.get("language", "pt"))

func add_coins(amount: int) -> void:
	total_coins += amount
	save_data()

func update_best_distance(distance: int) -> bool:
	if distance > best_distance:
		best_distance = distance
		save_data()
		return true
	return false
