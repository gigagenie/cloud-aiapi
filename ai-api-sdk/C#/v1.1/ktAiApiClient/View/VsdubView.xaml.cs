using System.Windows;
using System.Windows.Controls;

namespace AiApiTestClient.View
{
    /// <summary>
    /// VsdubView.xaml에 대한 상호 작용 논리
    /// </summary>
    public partial class VsdubView : UserControl
    {
        public VsdubView()
        {
            InitializeComponent();
        }
        private void TextBox_TextChanged3(object sender, TextChangedEventArgs e)
        {
            if (sender != null)
            {
                TextBox tb = sender as TextBox;

                if (DefaultText != null)
                {
                    if (string.IsNullOrEmpty(tb.Text))
                    {
                        DefaultText.Visibility = Visibility.Visible;
                    }
                    else
                    {
                        DefaultText.Visibility = Visibility.Hidden;
                    }
                }
            }
        }
    }
}
