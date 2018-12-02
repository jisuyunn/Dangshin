package com.example.yelimhan.bomtogether;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class FavoriteListAdapter extends ArrayAdapter {


    // 버튼 클릭 이벤트를 위한 Listener 인터페이스 정의.
    public interface ListBtnClickListener {
        void onListBtnClick(int position) ;
    }

    // 생성자로부터 전달된 resource id 값을 저장.
    int resourceId ;
    // 생성자로부터 전달된 ListBtnClickListener  저장.
    private ListBtnClickListener listBtnClickListener ;


    // 생성자. 마지막에 ListBtnClickListener 추가.
    FavoriteListAdapter(Context context, int resource, ArrayList<FavoriteListItem> list) {
        super(context, resource, list) ;

        // resource id 값 복사. (super로 전달된 resource를 참조할 방법이 없음.)
        this.resourceId = resource ;

    }

    // 새롭게 만든 Layout을 위한 View를 생성하는 코드
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final int pos = position ;
        final Context context = parent.getContext();

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(this.resourceId/*R.layout.listview_btn_item*/, parent, false);
        }

        final TextView textTextView = (TextView) convertView.findViewById(R.id.flisttext);

        final FavoriteListItem listViewItem = (FavoriteListItem) getItem(position);

        textTextView.setText(listViewItem.getText());

        Button button1 = (Button) convertView.findViewById(R.id.fbtn);
        button1.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                Toast.makeText(getContext(), Integer.toString(pos+1)+"번 아이템 눌림",Toast.LENGTH_SHORT).show();
                //textTextView.setText(Integer.toString(pos + 1) + "번 아이템 선택.");
            }
        });

//        // button2의 TAG에 position값 지정. Adapter를 click listener로 지정.
//        Button button2 = (Button) convertView.findViewById(R.id.button2);
//        button2.setTag(position);
//        button2.setOnClickListener(this);

        return convertView;
    }

//    // button2가 눌려졌을 때 실행되는 onClick함수.
//    public void onClick(View v) {
//        // ListBtnClickListener(MainActivity)의 onListBtnClick() 함수 호출.
//        if (this.listBtnClickListener != null) {
//            this.listBtnClickListener.onListBtnClick((int)v.getTag()) ;
//        }
//    }


}
