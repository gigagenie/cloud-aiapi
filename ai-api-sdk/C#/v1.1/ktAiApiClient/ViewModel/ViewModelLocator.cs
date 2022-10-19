using Microsoft.Extensions.DependencyInjection;
using Microsoft.Toolkit.Mvvm.DependencyInjection;

namespace AiApiTestClient.ViewModel
{
    public class ViewModelLocator
    {
        public ViewModelLocator()
        {
            Ioc.Default.ConfigureServices( new ServiceCollection()
                .AddSingleton<MainViewModel, MainViewModel>()
                .AddSingleton<AuthViewModel, AuthViewModel>()
                .AddSingleton<STTViewModel, STTViewModel>()
                .AddSingleton<STT2ViewModel, STT2ViewModel>()
                .AddSingleton<STTgRPCViewModel, STTgRPCViewModel>()
                .AddSingleton<TTSViewModel, TTSViewModel>()
                .AddSingleton<VsttsViewModel, VsttsViewModel>()
                .AddSingleton<VsdubViewModel, VsdubViewModel>()
                .AddSingleton<GenieMemoViewModel, GenieMemoViewModel>()
                .BuildServiceProvider());
        }

        #region ViewModel
        public MainViewModel Main { get => Ioc.Default.GetService<MainViewModel>(); }

        public STTViewModel STT { get => Ioc.Default.GetService<STTViewModel>(); }

        public STT2ViewModel STTAD { get => Ioc.Default.GetService<STT2ViewModel>(); }

        public STTgRPCViewModel STTgRPC { get => Ioc.Default.GetService<STTgRPCViewModel>(); }

        public TTSViewModel TTS { get => Ioc.Default.GetService<TTSViewModel>(); }

        public VsttsViewModel VSTTS { get => Ioc.Default.GetService<VsttsViewModel>(); }

        public VsdubViewModel VSDUB { get => Ioc.Default.GetService<VsdubViewModel>(); }

        public GenieMemoViewModel GenieMemo { get => Ioc.Default.GetService<GenieMemoViewModel>(); }

        public AuthViewModel AUTH { get => Ioc.Default.GetService<AuthViewModel>(); }
        #endregion
    }
}
