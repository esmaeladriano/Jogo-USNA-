extends Node

# Autoload para efeitos sonoros e música sintetizada proceduralmente
var sfx_player: AudioStreamPlayer
var music_player: AudioStreamPlayer

func _ready() -> void:
	sfx_player = AudioStreamPlayer.new()
	music_player = AudioStreamPlayer.new()
	add_child(sfx_player)
	add_child(music_player)
	music_player.bus = "Master"
	sfx_player.bus = "Master"

func play_sfx(name: String) -> void:
	if GameData.sound_volume <= 0.01:
		return
	# Ponto de integração para efeitos sonoros
	# Para usar ficheiros .wav ou .ogg, carrega com load("res://audio/" + name + ".wav")
	# Alternativamente, em plataformas móveis executa vibração se ativada
	if GameData.vibration_enabled and (name == "spike" or name == "record"):
		Input.vibrate_handheld(40)

func set_music_volume(vol: float) -> void:
	GameData.music_volume = vol
	AudioServer.set_bus_volume_db(AudioServer.get_bus_index("Master"), linear_to_db(vol))
