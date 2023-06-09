// Generated by view binder compiler. Do not edit!
package org.tensorflow.lite.examples.objectdetection.databinding;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewbinding.ViewBinding;
import androidx.viewbinding.ViewBindings;
import java.lang.NullPointerException;
import java.lang.Override;
import java.lang.String;
import org.tensorflow.lite.examples.objectdetection.R;

public final class ActivityTexttospeechBinding implements ViewBinding {
  @NonNull
  private final RelativeLayout rootView;

  @NonNull
  public final Button speakBtn;

  @NonNull
  public final Button stopBtn;

  @NonNull
  public final EditText textEt;

  private ActivityTexttospeechBinding(@NonNull RelativeLayout rootView, @NonNull Button speakBtn,
      @NonNull Button stopBtn, @NonNull EditText textEt) {
    this.rootView = rootView;
    this.speakBtn = speakBtn;
    this.stopBtn = stopBtn;
    this.textEt = textEt;
  }

  @Override
  @NonNull
  public RelativeLayout getRoot() {
    return rootView;
  }

  @NonNull
  public static ActivityTexttospeechBinding inflate(@NonNull LayoutInflater inflater) {
    return inflate(inflater, null, false);
  }

  @NonNull
  public static ActivityTexttospeechBinding inflate(@NonNull LayoutInflater inflater,
      @Nullable ViewGroup parent, boolean attachToParent) {
    View root = inflater.inflate(R.layout.activity_texttospeech, parent, false);
    if (attachToParent) {
      parent.addView(root);
    }
    return bind(root);
  }

  @NonNull
  public static ActivityTexttospeechBinding bind(@NonNull View rootView) {
    // The body of this method is generated in a way you would not otherwise write.
    // This is done to optimize the compiled bytecode for size and performance.
    int id;
    missingId: {
      id = R.id.speakBtn;
      Button speakBtn = ViewBindings.findChildViewById(rootView, id);
      if (speakBtn == null) {
        break missingId;
      }

      id = R.id.stopBtn;
      Button stopBtn = ViewBindings.findChildViewById(rootView, id);
      if (stopBtn == null) {
        break missingId;
      }

      id = R.id.textEt;
      EditText textEt = ViewBindings.findChildViewById(rootView, id);
      if (textEt == null) {
        break missingId;
      }

      return new ActivityTexttospeechBinding((RelativeLayout) rootView, speakBtn, stopBtn, textEt);
    }
    String missingId = rootView.getResources().getResourceName(id);
    throw new NullPointerException("Missing required view with ID: ".concat(missingId));
  }
}
