using CommonServiceLocator;
using Microsoft.Extensions.DependencyInjection;
using Microsoft.Toolkit.Mvvm.DependencyInjection;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace AiApiTestClient.ViewModel
{
    public class ViewModelLocator
    {
        public ViewModelLocator()
        {
            Ioc.Default.ConfigureServices( new ServiceCollection()
                .AddSingleton<MainViewModel, MainViewModel>()
                .AddSingleton<AuthViewModel, AuthViewModel>()
                .AddSingleton<STTRestApiViewModel, STTRestApiViewModel>()
                .AddSingleton<STTgRPCViewModel, STTgRPCViewModel>()
                .AddSingleton<TTSRestApiViewModel, TTSRestApiViewModel>()
                .BuildServiceProvider());
        }

        #region ViewModel
        public MainViewModel Main { get => Ioc.Default.GetService<MainViewModel>(); }

        public STTRestApiViewModel STT { get => Ioc.Default.GetService<STTRestApiViewModel>(); }

        public STTgRPCViewModel STTgRPC { get => Ioc.Default.GetService<STTgRPCViewModel>(); }

        public TTSRestApiViewModel TTS { get => Ioc.Default.GetService<TTSRestApiViewModel>(); }

        public AuthViewModel AUTH { get => Ioc.Default.GetService<AuthViewModel>(); }
        #endregion
    }
}
