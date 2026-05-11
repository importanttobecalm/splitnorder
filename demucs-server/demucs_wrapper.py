import sys
import torchaudio
import soundfile as sf
import torch

# KESİN ÇÖZÜM: torchaudio.save fonksiyonunu el ile soundfile'a yönlendir
# Bu sayede sorunlu DLL dosyaları (torchcodec/ffmpeg) hiç çağrılmayacak.
def mandatory_soundfile_save(filepath, src, sample_rate, encoding=None, bits_per_sample=None):
    print(f"[WRAPPER] Intercepted save for {filepath} using soundfile backend.")
    # Tensor'u numpy formatına çevir (Demucs çıktıları [channels, samples] formatındadır)
    data = src.t().cpu().numpy()
    sf.write(filepath, data, sample_rate)

# Orijinal fonksiyonu bizimkiyle değiştiriyoruz
torchaudio.save = mandatory_soundfile_save

from demucs.separate import main

if __name__ == "__main__":
    main()
